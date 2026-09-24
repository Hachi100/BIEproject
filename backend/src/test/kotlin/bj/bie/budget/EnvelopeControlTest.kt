package bj.bie.budget

import bj.bie.costing.EconomicTitle
import bj.bie.costing.FundingSource
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnvelopeControlTest {

    private val program = UUID.randomUUID()

    private fun envelope(category: EnvelopeCategory, ceiling: Long, reserved: Long = 0) = Envelope(
        UUID.randomUUID(), UUID.randomUUID(), 2026, program, "045", "Pilotage", category,
        BigDecimal(ceiling), BigDecimal(ceiling), BigDecimal(reserved), 1, "LF 2026",
    )

    @Test
    fun `les dépenses en capital sont ventilées selon la ressource`() {
        assertEquals(EnvelopeCategory.CAPITAL_DOMESTIC, EnvelopeCategory.of(EconomicTitle.CAPITAL, FundingSource.NATIONAL_BUDGET))
        assertEquals(EnvelopeCategory.CAPITAL_EXTERNAL_GRANTS, EnvelopeCategory.of(EconomicTitle.CAPITAL, FundingSource.GRANT))
        assertEquals(EnvelopeCategory.CAPITAL_EXTERNAL_LOANS, EnvelopeCategory.of(EconomicTitle.CAPITAL, FundingSource.LOAN))
        assertEquals(EnvelopeCategory.GOODS_AND_SERVICES, EnvelopeCategory.of(EconomicTitle.GOODS_AND_SERVICES, FundingSource.GRANT))
    }

    @Test
    fun `disponible = plafond − réservé − besoin`() {
        val needs = listOf(
            Need(program, EnvelopeCategory.GOODS_AND_SERVICES, BigDecimal(300)),
            Need(program, EnvelopeCategory.GOODS_AND_SERVICES, BigDecimal(200)),
        )
        val line = EnvelopeControl.check(needs, listOf(envelope(EnvelopeCategory.GOODS_AND_SERVICES, 1000, reserved = 400)), emptyMap()).single()
        assertEquals(BigDecimal(500), line.need)
        assertEquals(BigDecimal(100), line.available)
        assertFalse(line.overrun)
    }

    @Test
    fun `un besoin supérieur au disponible est un dépassement`() {
        val line = EnvelopeControl.check(
            listOf(Need(program, EnvelopeCategory.PERSONNEL, BigDecimal(1500))),
            listOf(envelope(EnvelopeCategory.PERSONNEL, 1000)),
            emptyMap(),
        ).single()
        assertTrue(line.overrun)
        assertEquals(BigDecimal(-500), line.available)
    }

    @Test
    fun `un besoin sans enveloppe est signalé comme tel`() {
        val line = EnvelopeControl.check(
            listOf(Need(program, EnvelopeCategory.CAPITAL_EXTERNAL_LOANS, BigDecimal(10))),
            listOf(envelope(EnvelopeCategory.GOODS_AND_SERVICES, 1000)),
            mapOf(program to ("045" to "Pilotage")),
        ).single()
        assertTrue(line.overrun)
        assertFalse(line.hasEnvelope)
        assertEquals("045", line.programCode)
    }
}
