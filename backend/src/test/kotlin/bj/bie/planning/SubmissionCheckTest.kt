package bj.bie.planning

import bj.bie.budget.EnvelopeCategory
import bj.bie.budget.EnvelopeCheckLine
import bj.bie.costing.ControlStatus
import bj.bie.costing.CostingLine
import bj.bie.costing.EconomicTitle
import bj.bie.costing.Finding
import bj.bie.costing.FindingLevel
import bj.bie.costing.FundingSource
import bj.bie.costing.PriceSource
import bj.bie.costing.ResourceNature
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubmissionCheckTest {

    private val programId = UUID.randomUUID()
    private val activity = OperationalActivity(
        id = UUID.randomUUID(), ptabId = UUID.randomUUID(), code = "045-001", label = "Revue du PTA",
        programId = programId, programCode = "045", responsibleUnit = "DPAF", expectedResult = null,
        startDate = LocalDate.of(2026, 3, 1), endDate = LocalDate.of(2026, 6, 30),
        executionMode = ExecutionMode.DIRECT, location = null,
    )
    private val task = Task(UUID.randomUUID(), activity.id, 1, "Atelier de revue", "SPSE", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), null)

    private fun line(status: ControlStatus = ControlStatus.COMPLIANT, homologation: Boolean = false) = CostingLine(
        id = UUID.randomUUID(), taskId = task.id, description = "Pause-café", resourceNature = ResourceNature.CATERING,
        quantity = BigDecimal(60), unit = "personne", frequency = BigDecimal(3), unitPrice = BigDecimal(2500),
        total = BigDecimal(450000), priceSource = PriceSource.OFFICIAL_SCALE, priceReferenceCode = null,
        economicTitle = EconomicTitle.GOODS_AND_SERVICES, economicTitleOrigin = "DECLARED",
        fundingSource = FundingSource.NATIONAL_BUDGET, justification = null, controlStatus = status,
        controlFindings = if (status == ControlStatus.BLOCKING) listOf(Finding("PRIX_SUPERIEUR_A_LA_BS", FindingLevel.BLOCKING, "trop cher")) else emptyList(),
        homologationRequired = homologation,
    )

    private fun envelope(need: Long, ceiling: Long, exists: Boolean = true) = EnvelopeCheckLine(
        programId, "045", "Pilotage", EnvelopeCategory.GOODS_AND_SERVICES,
        BigDecimal(ceiling), BigDecimal.ZERO, BigDecimal(need), BigDecimal(ceiling - need), need > ceiling, exists,
    )

    @Test
    fun `un PTAB complet, chiffré et dans l'enveloppe est soumissible`() {
        val report = SubmissionCheck.run(2026, listOf(activity), listOf(task), listOf(line()), listOf(envelope(450000, 1_000_000)))
        assertTrue(report.submittable)
        assertTrue(report.warnings.isEmpty())
    }

    @Test
    fun `un PTAB vide n'est pas soumissible`() {
        assertEquals(listOf("PTAB_VIDE"), SubmissionCheck.run(2026, emptyList(), emptyList(), emptyList(), emptyList()).blocking.map { it.code })
    }

    @Test
    fun `une activité sans tâche ou sans costing bloque la soumission`() {
        assertEquals(listOf("ACTIVITE_SANS_TACHE"), SubmissionCheck.run(2026, listOf(activity), emptyList(), emptyList(), emptyList()).blocking.map { it.code })
        assertEquals(listOf("ACTIVITE_SANS_COSTING"), SubmissionCheck.run(2026, listOf(activity), listOf(task), emptyList(), emptyList()).blocking.map { it.code })
    }

    @Test
    fun `les dates doivent respecter l'exercice et la période de l'activité`() {
        val lateActivity = activity.copy(endDate = LocalDate.of(2027, 1, 15))
        val lateTask = task.copy(endDate = LocalDate.of(2026, 7, 10))
        val codes = SubmissionCheck.run(2026, listOf(lateActivity), listOf(lateTask), listOf(line()), emptyList()).blocking.map { it.code }
        assertEquals(listOf("ACTIVITE_HORS_EXERCICE"), codes)
        val codes2 = SubmissionCheck.run(2026, listOf(activity), listOf(lateTask), listOf(line()), emptyList()).blocking.map { it.code }
        assertEquals(listOf("TACHE_HORS_PERIODE_ACTIVITE"), codes2)
    }

    @Test
    fun `une ligne bloquante et un dépassement d'enveloppe empêchent la soumission`() {
        val report = SubmissionCheck.run(2026, listOf(activity), listOf(task), listOf(line(ControlStatus.BLOCKING)), listOf(envelope(450000, 100000)))
        assertEquals(setOf("LIGNE_DE_COUT_BLOQUANTE", "DEPASSEMENT_ENVELOPPE"), report.blocking.map { it.code }.toSet())
    }

    @Test
    fun `un besoin sans enveloppe ouverte est un dépassement`() {
        val report = SubmissionCheck.run(2026, listOf(activity), listOf(task), listOf(line()), listOf(envelope(450000, 0, exists = false)))
        assertTrue(report.blocking.single().message.contains("aucune enveloppe"))
    }

    @Test
    fun `l'homologation requise est un avertissement, pas un blocage`() {
        val report = SubmissionCheck.run(2026, listOf(activity), listOf(task), listOf(line(homologation = true)), emptyList())
        assertTrue(report.submittable)
        assertEquals(listOf("HOMOLOGATION_REQUISE"), report.warnings.map { it.code })
    }
}
