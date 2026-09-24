package bj.bie.budget

import bj.bie.audit.AuditJournal
import bj.bie.shared.Caller
import bj.bie.shared.InvalidRequestException
import bj.bie.shared.NotFoundException
import bj.bie.shared.toFcfa
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class BudgetService(private val budget: BudgetRepository, private val audit: AuditJournal) {

    fun envelopes(caller: Caller, fiscalYear: Int): List<Envelope> = budget.envelopes(caller.tenantId, fiscalYear)

    /** Contrôle besoin / enveloppe d'un PTAB (l'appartenance du PTAB est vérifiée par l'appelant). */
    fun checkPtab(tenantId: UUID, ptabId: UUID, fiscalYear: Int, programLabels: Map<UUID, Pair<String, String>>): List<EnvelopeCheckLine> =
        EnvelopeControl.check(budget.needsOfPtab(tenantId, ptabId), budget.envelopes(tenantId, fiscalYear), programLabels)

    /** Une révision de plafond est versionnée, justifiée et auditée (Partie 4 §36). */
    @Transactional
    fun reviseCeiling(caller: Caller, envelopeId: UUID, newCeiling: BigDecimal, justification: String): Envelope {
        if (justification.isBlank()) throw InvalidRequestException("Une révision de plafond doit être justifiée.")
        if (newCeiling.signum() < 0) throw InvalidRequestException("Le plafond ne peut pas être négatif.")
        val envelope = budget.lockEnvelope(caller.tenantId, envelopeId)
            ?: throw NotFoundException("Enveloppe $envelopeId introuvable pour l'institution ${caller.tenantCode}.")
        val ceiling = newCeiling.toFcfa()
        budget.reviseCeiling(envelope, ceiling, justification, caller.userId)
        val revised = envelope.copy(currentCeiling = ceiling, version = envelope.version + 1)
        audit.record(
            tenantId = caller.tenantId,
            actor = caller.userId,
            action = "ENVELOPE_CEILING_REVISED",
            objectType = "BudgetEnvelope",
            objectId = envelope.id,
            before = mapOf("currentCeiling" to envelope.currentCeiling, "version" to envelope.version),
            after = mapOf("currentCeiling" to ceiling, "version" to revised.version),
            justification = justification,
        )
        return revised
    }
}
