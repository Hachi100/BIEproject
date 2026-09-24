package bj.bie.budget

import bj.bie.costing.EconomicTitle
import bj.bie.costing.FundingSource
import java.math.BigDecimal
import java.util.UUID

/**
 * Catégories des enveloppes, alignées sur la présentation de la loi de finances
 * (dépenses ordinaires par titre, dépenses en capital par nature de ressources).
 */
enum class EnvelopeCategory {
    PERSONNEL,
    GOODS_AND_SERVICES,
    TRANSFERS,
    CAPITAL_DOMESTIC,
    CAPITAL_EXTERNAL,
    CAPITAL_EXTERNAL_GRANTS,
    CAPITAL_EXTERNAL_LOANS;

    companion object {
        fun of(title: EconomicTitle, funding: FundingSource): EnvelopeCategory = when (title) {
            EconomicTitle.PERSONNEL -> PERSONNEL
            EconomicTitle.GOODS_AND_SERVICES -> GOODS_AND_SERVICES
            EconomicTitle.TRANSFERS -> TRANSFERS
            EconomicTitle.CAPITAL -> when (funding) {
                FundingSource.GRANT -> CAPITAL_EXTERNAL_GRANTS
                FundingSource.LOAN -> CAPITAL_EXTERNAL_LOANS
                FundingSource.NATIONAL_BUDGET, FundingSource.COUNTERPART, FundingSource.SPECIAL_FUND -> CAPITAL_DOMESTIC
            }
        }
    }
}

data class Envelope(
    val id: UUID,
    val tenantId: UUID,
    val fiscalYear: Int,
    val programId: UUID,
    val programCode: String,
    val programLabel: String,
    val category: EnvelopeCategory,
    val initialCeiling: BigDecimal,
    val currentCeiling: BigDecimal,
    val reserved: BigDecimal,
    val version: Int,
    val source: String,
)

/** Besoin chiffré d'un PTAB agrégé par programme et catégorie. */
data class Need(val programId: UUID, val category: EnvelopeCategory, val amount: BigDecimal)

data class EnvelopeCheckLine(
    val programId: UUID,
    val programCode: String,
    val programLabel: String,
    val category: EnvelopeCategory,
    val currentCeiling: BigDecimal,
    val reserved: BigDecimal,
    val need: BigDecimal,
    val available: BigDecimal,
    val overrun: Boolean,
    val hasEnvelope: Boolean,
)

/**
 * Contrôle besoin / enveloppe (Partie 4 §13-17) : Besoin ≠ Plafond ≠ Allocation.
 * Disponible = plafond courant − réservé − besoin du PTAB. Un besoin sans enveloppe
 * correspondante est un dépassement.
 */
object EnvelopeControl {

    fun check(needs: List<Need>, envelopes: List<Envelope>, programLabels: Map<UUID, Pair<String, String>>): List<EnvelopeCheckLine> {
        val needsByKey = needs.groupBy { it.programId to it.category }.mapValues { (_, v) -> v.fold(BigDecimal.ZERO) { a, n -> a + n.amount } }
        val envelopesByKey = envelopes.associateBy { it.programId to it.category }
        return needsByKey.map { (key, need) ->
            val envelope = envelopesByKey[key]
            val ceiling = envelope?.currentCeiling ?: BigDecimal.ZERO
            val reserved = envelope?.reserved ?: BigDecimal.ZERO
            val available = ceiling - reserved - need
            val (code, label) = envelope?.let { it.programCode to it.programLabel } ?: programLabels[key.first] ?: ("?" to "?")
            EnvelopeCheckLine(
                programId = key.first,
                programCode = code,
                programLabel = label,
                category = key.second,
                currentCeiling = ceiling,
                reserved = reserved,
                need = need,
                available = available,
                overrun = available.signum() < 0,
                hasEnvelope = envelope != null,
            )
        }.sortedWith(compareBy({ it.programCode }, { it.category }))
    }
}
