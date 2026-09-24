package bj.bie.costing

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PriceControlTest {

    private val today = LocalDate.of(2026, 9, 24)

    private fun reference(
        lower: String? = "970",
        upper: String? = "1250",
        family: String = "MATIÈRES, MATÉRIEL ET FOURNITURES",
        designation: String = "SCOTCH ADHESIF TRANSPARENT GM",
        code: String = "6013 3321 251 1111",
        editionDate: LocalDate = LocalDate.of(2026, 6, 1),
        flags: List<String> = emptyList(),
    ) = PriceReference(
        id = UUID.randomUUID(),
        code = code,
        family = family,
        designation = designation,
        specifications = null,
        unit = "U",
        lowerBound = lower?.let(::BigDecimal),
        upperBound = upper?.let(::BigDecimal),
        edition = "19e édition v26.3",
        editionDate = editionDate,
        sourcePage = 401,
        qualityFlags = flags,
    )

    private fun check(price: String, reference: PriceReference?, nature: ResourceNature = ResourceNature.SUPPLIES, source: PriceSource = PriceSource.PRICE_REPERTOIRE) =
        PriceControl.check(BigDecimal(price), source, nature, reference, today)

    @Test
    fun `un prix dans l'intervalle du répertoire est conforme`() {
        val result = check("1100", reference())
        assertEquals(ControlStatus.COMPLIANT, result.status)
        assertTrue(result.findings.isEmpty())
    }

    @Test
    fun `la borne supérieure est incluse`() {
        assertEquals(ControlStatus.COMPLIANT, check("1250", reference()).status)
    }

    @Test
    fun `dépasser la borne supérieure est bloquant`() {
        val result = check("1500", reference())
        assertEquals(ControlStatus.BLOCKING, result.status)
        assertEquals("PRIX_SUPERIEUR_A_LA_BS", result.findings.single().code)
        assertEquals(BigDecimal("20.00"), result.deviationFromUpperBoundPercent)
    }

    @Test
    fun `un prix sous la borne inférieure déclenche une alerte de sous-évaluation`() {
        val result = check("500", reference())
        assertEquals(ControlStatus.WARNING, result.status)
        assertEquals("PRIX_INFERIEUR_A_LA_BI", result.findings.single().code)
    }

    @Test
    fun `un article hors répertoire exige une homologation CERPR pour les fournitures`() {
        val result = check("25000", null, source = PriceSource.MARKET_OBSERVATION)
        assertEquals(ControlStatus.WARNING, result.status)
        assertTrue(result.homologationRequired)
        assertEquals("ARTICLE_HORS_REPERTOIRE", result.findings.single().code)
    }

    @Test
    fun `les frais de mission ne relèvent pas du répertoire des prix`() {
        val result = check("30000", null, nature = ResourceNature.MISSION_ALLOWANCE, source = PriceSource.OFFICIAL_SCALE)
        assertEquals(ControlStatus.COMPLIANT, result.status)
        assertFalse(result.homologationRequired)
    }

    @Test
    fun `une borne illisible dans la source n'est pas utilisée pour bloquer`() {
        val result = check("99999999", reference(upper = null, flags = listOf("BS_ILLISIBLE_DANS_LA_SOURCE")))
        assertEquals(ControlStatus.WARNING, result.status)
        assertEquals("BORNE_SUPERIEURE_INDISPONIBLE", result.findings.single().code)
    }

    @Test
    fun `des bornes incohérentes dans la source ne sont pas appliquées`() {
        // cas réel de la v26.3 : SUV HONDA CR-V, BI = 30 769 231 et BS publiée « 32 »
        val result = check("31000000", reference(lower = "30769231", upper = "32", flags = listOf("BI_SUPERIEURE_A_BS")))
        assertEquals(ControlStatus.WARNING, result.status)
        assertEquals(listOf("BORNE_SUPERIEURE_INDISPONIBLE"), result.findings.map { it.code })
    }

    @Test
    fun `le matériel informatique requiert l'avis préalable du DSI`() {
        val result = check(
            "700000",
            reference(lower = "600000", upper = "800000", family = "MATÉRIEL INFORMATIQUE ET DE COMMUNICATION", designation = "ORDINATEUR PORTABLE PROFIL 1"),
            nature = ResourceNature.EQUIPMENT,
        )
        assertEquals(ControlStatus.COMPLIANT, result.status)
        assertTrue(result.findings.any { it.code == "AVIS_PREALABLE_REQUIS" && it.message.contains("DSI") })
    }

    @Test
    fun `une référence de plus de douze mois est signalée`() {
        val result = check("1100", reference(editionDate = LocalDate.of(2025, 1, 1)))
        assertEquals(ControlStatus.WARNING, result.status)
        assertEquals("REFERENCE_ANCIENNE", result.findings.single().code)
    }

    @Test
    fun `l'imputation est suggérée par la classe du code article`() {
        assertEquals(EconomicTitle.GOODS_AND_SERVICES, reference(code = "6013 3321 251 1111").suggestedEconomicTitle())
        assertEquals(EconomicTitle.CAPITAL, reference(code = "2421 1111 128 1114").suggestedEconomicTitle())
    }

    @Test
    fun `le total est quantité × fréquence × prix, arrondi au franc`() {
        // 60 participants × 3 jours × 2 500,50 FCFA
        assertEquals(BigDecimal("450090"), CostingCalculator.total(BigDecimal(60), BigDecimal(3), BigDecimal("2500.50")))
    }
}
