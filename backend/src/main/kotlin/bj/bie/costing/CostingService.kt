package bj.bie.costing

import bj.bie.audit.AuditJournal
import bj.bie.planning.PlanningRepository
import bj.bie.shared.Caller
import bj.bie.shared.InvalidRequestException
import bj.bie.shared.InvalidStateException
import bj.bie.shared.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

data class NewCostingLine(
    val description: String,
    val resourceNature: ResourceNature,
    val quantity: BigDecimal,
    val unit: String,
    val frequency: BigDecimal = BigDecimal.ONE,
    val unitPrice: BigDecimal,
    val priceSource: PriceSource,
    val priceReferenceCode: String? = null,
    val economicTitle: EconomicTitle? = null,
    val fundingSource: FundingSource = FundingSource.NATIONAL_BUDGET,
    val justification: String? = null,
)

@Service
class CostingService(
    private val costing: CostingRepository,
    private val planning: PlanningRepository,
    private val audit: AuditJournal,
    private val clock: Clock,
) {

    fun searchPriceReferences(query: String, limit: Int): List<PriceReference> {
        if (query.isBlank()) throw InvalidRequestException("Saisir au moins un mot-clé ou un code d'article.")
        return costing.searchPriceReferences(query.trim(), limit.coerceIn(1, 100))
    }

    fun priceReference(code: String): PriceReference =
        costing.findPriceReference(code) ?: throw NotFoundException("Article $code absent du répertoire des prix.")

    /**
     * Ajoute une ligne de costing à une tâche : calcule le total, contrôle le prix contre le
     * répertoire et enregistre le résultat du contrôle avec la ligne. Une ligne bloquante est
     * conservée (l'utilisateur voit le problème) mais empêchera la soumission du PTAB.
     */
    @Transactional
    fun addLine(caller: Caller, taskId: UUID, command: NewCostingLine): CostingLine {
        val context = planning.taskContext(caller.tenantId, taskId)
            ?: throw NotFoundException("Tâche $taskId introuvable pour l'institution ${caller.tenantCode}.")
        if (!context.ptabStatus.editable) {
            throw InvalidStateException("Le PTAB est à l'état ${context.ptabStatus} : le costing ne peut plus être modifié.")
        }
        if (command.description.isBlank() || command.unit.isBlank()) {
            throw InvalidRequestException("La désignation et l'unité de la ressource sont obligatoires.")
        }
        if (command.quantity.signum() <= 0 || command.frequency.signum() <= 0) {
            throw InvalidRequestException("La quantité et la fréquence doivent être strictement positives.")
        }
        if (command.unitPrice.signum() < 0) throw InvalidRequestException("Le prix unitaire ne peut pas être négatif.")

        val reference = command.priceReferenceCode?.takeIf { it.isNotBlank() }?.let { priceReference(it.trim()) }
        if (command.priceSource == PriceSource.PRICE_REPERTOIRE && reference == null) {
            throw InvalidRequestException("Un prix issu du répertoire doit indiquer le code de l'article (provenance obligatoire).")
        }
        val suggested = reference?.suggestedEconomicTitle()
        val economicTitle = command.economicTitle ?: suggested
            ?: throw InvalidRequestException("Préciser la nature économique de la dépense (imputation) : elle ne peut pas être déduite.")
        val control = PriceControl.check(command.unitPrice, command.priceSource, command.resourceNature, reference, LocalDate.now(clock))

        val line = CostingLine(
            id = UUID.randomUUID(),
            taskId = taskId,
            description = command.description.trim(),
            resourceNature = command.resourceNature,
            quantity = command.quantity,
            unit = command.unit.trim(),
            frequency = command.frequency,
            unitPrice = command.unitPrice,
            total = CostingCalculator.total(command.quantity, command.frequency, command.unitPrice),
            priceSource = command.priceSource,
            priceReferenceCode = reference?.code,
            economicTitle = economicTitle,
            economicTitleOrigin = if (command.economicTitle != null) "DECLARED" else "INFERRED_FROM_PRICE_REFERENCE",
            fundingSource = command.fundingSource,
            justification = command.justification?.trim()?.ifEmpty { null },
            controlStatus = control.status,
            controlFindings = control.findings,
            homologationRequired = control.homologationRequired,
        )
        costing.insert(caller.tenantId, line, reference?.id)
        audit.record(
            tenantId = caller.tenantId,
            actor = caller.userId,
            action = "COSTING_LINE_ADDED",
            objectType = "CostingLine",
            objectId = line.id,
            after = line,
        )
        return line
    }

    /** Retire une ligne de coût tant que le PTAB est modifiable ; la suppression est auditée. */
    @Transactional
    fun deleteLine(caller: Caller, lineId: UUID, justification: String?) {
        val line = costing.findLine(caller.tenantId, lineId)
            ?: throw NotFoundException("Ligne de coût $lineId introuvable pour l'institution ${caller.tenantCode}.")
        val context = planning.taskContext(caller.tenantId, line.taskId)
            ?: throw NotFoundException("Tâche ${line.taskId} introuvable.")
        if (!context.ptabStatus.editable) {
            throw InvalidStateException("Le PTAB est à l'état ${context.ptabStatus} : le costing ne peut plus être modifié.")
        }
        costing.delete(caller.tenantId, lineId)
        audit.record(
            tenantId = caller.tenantId,
            actor = caller.userId,
            action = "COSTING_LINE_DELETED",
            objectType = "CostingLine",
            objectId = lineId,
            before = line,
            justification = justification,
        )
    }
}
