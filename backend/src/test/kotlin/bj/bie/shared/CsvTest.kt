package bj.bie.shared

import java.io.StringReader
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvTest {

    @Test
    fun `lit les champs entre guillemets, les virgules et retours à la ligne internes`() {
        val text = "code,designation,unite\r\n" +
            "1,\"ARMOIRE, 3 BATTANTS\",U\n" +
            "2,\"ECRAN 24\"\" LED\nFULL HD\",U\n"
        val records = Csv.readRecords(StringReader(text))
        assertEquals(2, records.size)
        assertEquals("ARMOIRE, 3 BATTANTS", records[0]["designation"])
        assertEquals("ECRAN 24\" LED\nFULL HD", records[1]["designation"])
    }

    @Test
    fun `montants arrondis au franc et conversion hors taxes`() {
        assertEquals(BigDecimal("1251"), BigDecimal("1250.50").toFcfa())
        assertEquals(BigDecimal("1000.00"), BigDecimal("1180").excludingVat())
    }
}
