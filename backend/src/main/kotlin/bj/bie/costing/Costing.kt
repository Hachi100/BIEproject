package bj.bie.costing

import bj.bie.shared.toFcfa
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/** Déterminant de coût (Partie 2 §13, Partie 4 §4). */
enum class ResourceNature(val coveredByPriceRepertoire: Boolean) {
    PERSONNEL(false),
    MISSION_ALLOWANCE(false),
    TRANSPORT(false),
    CATERING(false),
    ACCOMMODATION(false),
    SUPPLIES(true),
    EQUIPMENT(true),
    SERVICES(true),
    WORKS(true),
    TRANSFER(false),
    OTHER(false),
}

/** Provenance d'un prix (Partie 4 §5) : tout prix important doit avoir une source. */
enum class PriceSource {
    PRICE_REPERTOIRE,
    SYCOREF,
    HISTORICAL_CONTRACT,
    APPROVED_INTERNAL_PRICE,
    MARKET_OBSERVATION,
    TECHNICAL_ESTIMATE,
    OFFICIAL_SCALE,
}

/** Titres de la nomenclature économique (directive UEMOA n°08/2009). */
enum class EconomicTitle { PERSONNEL, GOODS_AND_SERVICES, TRANSFERS, CAPITAL }

enum class FundingSource { NATIONAL_BUDGET, GRANT, LOAN, COUNTERPART, SPECIAL_FUND }

enum class ControlStatus { COMPLIANT, WARNING, BLOCKING }

enum class FindingLevel { INFO, WARNING, BLOCKING }

data class Finding(val code: String, val level: FindingLevel, val message: String)

data class PriceReference(
    val id: UUID,
    val code: String,
    val family: String,
    val designation: String,
    val specifications: String?,
    val unit: String?,
    val lowerBound: BigDecimal?,
    val upperBound: BigDecimal?,
    val edition: String,
    val editionDate: LocalDate,
    val sourcePage: Int?,
    val qualityFlags: List<String>,
) {
    /**
     * Imputation suggérée à partir du code de l'article, arrimé à la nomenclature budgétaire :
     * classe 2 (immobilisations) -> investissement, classe 6 (charges) -> biens et services.
     * Heuristique à confirmer avec la DNCF ; l'origine « déduite » est tracée sur la ligne.
     */
    fun suggestedEconomicTitle(): EconomicTitle? = when (code.firstOrNull()) {
        '2' -> EconomicTitle.CAPITAL
        '6' -> EconomicTitle.GOODS_AND_SERVICES
        else -> null
    }
}

data class PriceControlResult(
    val status: ControlStatus,
    val findings: List<Finding>,
    val homologationRequired: Boolean,
    /** Écart du prix proposé par rapport à la borne supérieure, en % (positif = au-dessus). */
    val deviationFromUpperBoundPercent: BigDecimal?,
)

/**
 * Contrôle d'un prix unitaire selon le guide d'utilisation de l'e-Répertoire des prix de
 * référence (19e édition) :
 *  - les utilisateurs ne sont pas autorisés à dépasser la borne supérieure (BS) ;
 *  - un article hors répertoire est soumis à l'avis préalable de la CERPR (homologation
 *    sur la plateforme e-Répertoire, normalement au 4e trimestre de N pour N+1) ;
 *  - matériel informatique et mobilier : avis préalables DSI et DGML requis.
 * Le contrôle ne décide pas : il qualifie la ligne (conforme, alerte, bloquante).
 */
object PriceControl {

    private const val MAX_REFERENCE_AGE_MONTHS = 12L

    fun check(
        unitPrice: BigDecimal,
        priceSource: PriceSource,
        nature: ResourceNature,
        reference: PriceReference?,
        today: LocalDate,
    ): PriceControlResult {
        val findings = mutableListOf<Finding>()
        var homologationRequired = false
        var deviation: BigDecimal? = null

        if (reference == null) {
            if (nature.coveredByPriceRepertoire) {
                homologationRequired = true
                findings += Finding(
                    "ARTICLE_HORS_REPERTOIRE",
                    FindingLevel.WARNING,
                    "Article hors répertoire des prix : une demande d'homologation doit être soumise à la CERPR " +
                        "sur la plateforme e-Répertoire avant l'inscription dans les dossiers de concurrence.",
                )
            }
        } else {
            findings += boundFindings(reference, unitPrice)
            deviation = reference.upperBound?.takeIf { it.signum() > 0 }?.let {
                unitPrice.subtract(it).multiply(BigDecimal(100)).divide(it, 2, RoundingMode.HALF_UP)
            }
            findings += priorOpinionFindings(reference)
            val ageMonths = ChronoUnit.MONTHS.between(reference.editionDate, today)
            if (ageMonths > MAX_REFERENCE_AGE_MONTHS) {
                findings += Finding(
                    "REFERENCE_ANCIENNE",
                    FindingLevel.WARNING,
                    "Prix de référence issu de l'édition « ${reference.edition} », vieille de $ageMonths mois : vérifier la version en vigueur.",
                )
            }
            if (priceSource != PriceSource.PRICE_REPERTOIRE && priceSource != PriceSource.SYCOREF) {
                findings += Finding(
                    "SOURCE_DIFFERENTE_DU_REPERTOIRE",
                    FindingLevel.INFO,
                    "L'article existe au répertoire mais le prix est déclaré avec la source $priceSource.",
                )
            }
        }

        val status = when {
            findings.any { it.level == FindingLevel.BLOCKING } -> ControlStatus.BLOCKING
            findings.any { it.level == FindingLevel.WARNING } -> ControlStatus.WARNING
            else -> ControlStatus.COMPLIANT
        }
        return PriceControlResult(status, findings, homologationRequired, deviation)
    }

    private fun boundFindings(reference: PriceReference, unitPrice: BigDecimal): List<Finding> {
        val findings = mutableListOf<Finding>()
        val upper = reference.upperBound
        val lower = reference.lowerBound
        val unreliable = reference.qualityFlags.any { it.startsWith("BS_") || it == "BI_SUPERIEURE_A_BS" }
        when {
            upper == null || unreliable -> findings += Finding(
                "BORNE_SUPERIEURE_INDISPONIBLE",
                FindingLevel.WARNING,
                "La borne supérieure de l'article ${reference.code} est absente ou illisible dans l'édition " +
                    "« ${reference.edition} » (p. ${reference.sourcePage}) : vérifier le prix sur e-Répertoire.",
            )
            unitPrice > upper -> findings += Finding(
                "PRIX_SUPERIEUR_A_LA_BS",
                FindingLevel.BLOCKING,
                "Prix unitaire ${unitPrice.toFcfa()} FCFA supérieur à la borne supérieure ${upper.toFcfa()} FCFA " +
                    "de l'article ${reference.code} : le répertoire interdit de dépasser la BS.",
            )
        }
        if (lower != null && unitPrice < lower && !unreliable) {
            findings += Finding(
                "PRIX_INFERIEUR_A_LA_BI",
                FindingLevel.WARNING,
                "Prix unitaire ${unitPrice.toFcfa()} FCFA inférieur à la borne inférieure ${lower.toFcfa()} FCFA : " +
                    "risque de sous-évaluation du besoin et d'infructuosité de la procédure.",
            )
        }
        return findings
    }

    private fun priorOpinionFindings(reference: PriceReference): List<Finding> {
        val family = reference.family.uppercase()
        val designation = reference.designation.uppercase()
        val opinions = buildList {
            if (family.contains("INFORMATIQUE") || designation.startsWith("ORDINATEUR") || designation.startsWith("IMPRIMANTE")) {
                add("du Directeur des Systèmes d'Information (DSI)")
            }
            if (family.contains("MOBILIER")) add("de la Direction Générale du Matériel et de la Logistique (DGML)")
        }
        return opinions.map {
            Finding(
                "AVIS_PREALABLE_REQUIS",
                FindingLevel.INFO,
                "Commande soumise à l'avis préalable $it, selon le profil de l'utilisateur.",
            )
        }
    }
}

/** Ligne de costing : Tâche → Ressource → Quantité × Fréquence × Prix unitaire (Partie 4 §3). */
object CostingCalculator {
    fun total(quantity: BigDecimal, frequency: BigDecimal, unitPrice: BigDecimal): BigDecimal =
        quantity.multiply(frequency).multiply(unitPrice).toFcfa()
}
