package bj.bie

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Les données officielles chargées par les migrations sont complètes et historisées. */
class ReferenceDataTest : IntegrationTest() {

    @Autowired lateinit var jdbc: JdbcClient
    @Autowired lateinit var mvc: MockMvc

    private fun count(sql: String): Long = jdbc.sql(sql).query(Long::class.java).single()

    @Test
    fun `le répertoire des prix v26_3 est chargé intégralement`() {
        assertEquals(10_093, count("SELECT count(*) FROM costing.price_reference WHERE edition = '19e édition v26.3'"))
        // les anomalies de la source sont conservées, pas corrigées
        assertTrue(count("SELECT count(*) FROM costing.price_reference WHERE 'BS_ILLISIBLE_DANS_LA_SOURCE' = ANY(quality_flags)") > 0)
    }

    @Test
    fun `les 107 programmes et dotations de la LF 2026 sont chargés avec leurs crédits`() {
        assertEquals(107, count("SELECT count(*) FROM referential.program"))
        assertTrue(count("SELECT count(*) FROM budget.program_allocation") > 2000)
        assertTrue(count("SELECT count(*) FROM budget.envelope WHERE fiscal_year = 2026") > 300)
    }

    @Test
    fun `les restructurations ministérielles sont historisées`() {
        // Ministère des Infrastructures et des Transports : crédits jusqu'en 2023 seulement
        val mitEnd = jdbc.sql("SELECT valid_to_year FROM referential.organization WHERE code = 'MIT'").query(Int::class.java).single()
        assertEquals(2023, mitEnd)
        assertEquals(1, count("SELECT count(*) FROM referential.organization WHERE code = 'MS' AND valid_to_year IS NULL"))
        // le code 078 a désigné deux programmes différents : le référentiel les distingue par période
        assertEquals(2, count("SELECT count(*) FROM referential.program WHERE code = '078'"))
    }

    @Test
    fun `l'API liste les programmes actifs d'une institution pour un exercice`() {
        mvc.get("/api/v1/referential/programs?year=2026&organization=MS").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(3) }
            jsonPath("$[?(@.code == '045')].label") { exists() }
        }
    }

    @Test
    fun `la recherche plein texte du répertoire trouve un article et donne le prix hors taxes`() {
        mvc.get("/api/v1/costing/price-references?q=scotch adhesif transparent").andExpect {
            status { isOk() }
            jsonPath("$[0].reference.code") { value("6013 3321 251 1111") }
            jsonPath("$[0].reference.upperBound") { value(1250) }
        }
    }
}
