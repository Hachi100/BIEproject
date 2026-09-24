package db.migration

import bj.bie.shared.Csv
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.util.UUID

/**
 * Charge les institutions, programmes et crédits 2022-2028 publiés par la DGB
 * (« Tableaux de classifications croisées », budget LF 2026).
 *
 * La période de validité d'un programme est déduite des années où il porte des crédits :
 * elle reste ouverte (valid_to_year NULL) si le programme a des crédits en 2028, dernière
 * année de la source. Ceci matérialise les restructurations (ex. MIT et ME jusqu'en 2023).
 */
@Suppress("ClassName")
class V5__Seed_referential_lf2026 : BaseJavaMigration() {

    override fun migrate(context: Context) {
        val programs = readCsv("db/seed/programmes_budgetaires_lf2026.csv")
        val credits = readCsv("db/seed/credits_programmes_2022_2028.csv")
        val connection = context.connection

        val organizationIds = insertOrganizations(connection, programs)
        val programIdsBySourceRow = insertPrograms(connection, programs, organizationIds)
        insertAllocations(connection, credits, programIdsBySourceRow)
    }

    private fun insertOrganizations(connection: Connection, programs: List<Map<String, String>>): Map<String, UUID> {
        val ids = mutableMapOf<String, UUID>()
        val byOrganization = programs.groupBy { organizationKey(it) }
        connection.prepareStatement(
            """
            INSERT INTO referential.organization (id, code, name, type, valid_from_year, valid_to_year, source)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
        ).use { statement ->
            byOrganization.forEach { (key, rows) ->
                val first = rows.first()
                val id = UUID.randomUUID()
                val (fromYear, toYear) = validity(rows)
                statement.setObject(1, id)
                statement.setString(2, organizationCode(first))
                statement.setString(3, organizationName(first))
                statement.setString(4, organizationType(first))
                statement.setInt(5, fromYear)
                statement.setObject(6, toYear)
                statement.setString(7, SOURCE)
                statement.addBatch()
                ids[key] = id
            }
            statement.executeBatch()
        }
        return ids
    }

    private fun insertPrograms(
        connection: Connection,
        programs: List<Map<String, String>>,
        organizationIds: Map<String, UUID>,
    ): Map<String, UUID> {
        val ids = mutableMapOf<String, UUID>()
        connection.prepareStatement(
            """
            INSERT INTO referential.program
                (id, code, source_code, label, type, organization_id, valid_from_year, valid_to_year, source, source_row, quality_flags)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
        ).use { statement ->
            programs.forEach { row ->
                val id = UUID.randomUUID()
                val (fromYear, toYear) = validity(listOf(row))
                val flags = row.getValue("anomalies").split('|').filter { it.isNotBlank() }.toMutableList()
                if (row.getValue("premiere_annee_credits").isBlank()) flags += "SANS_CREDITS"
                statement.setObject(1, id)
                statement.setString(2, row.getValue("code"))
                statement.setString(3, row.getValue("code_source"))
                statement.setString(4, row.getValue("libelle"))
                statement.setString(5, if (row.getValue("type") == "DOTATION") "BUDGET_ALLOCATION" else "PROGRAM")
                statement.setObject(6, organizationIds.getValue(organizationKey(row)))
                statement.setInt(7, fromYear)
                statement.setObject(8, toYear)
                statement.setString(9, SOURCE)
                statement.setInt(10, row.getValue("ligne_source").toInt())
                statement.setArray(11, connection.createArrayOf("text", flags.toTypedArray()))
                statement.addBatch()
                ids[row.getValue("ligne_source")] = id
            }
            statement.executeBatch()
        }
        return ids
    }

    private fun insertAllocations(connection: Connection, credits: List<Map<String, String>>, programIds: Map<String, UUID>) {
        connection.prepareStatement(
            "INSERT INTO budget.program_allocation (fiscal_year, program_id, category, amount, source) VALUES (?, ?, ?, ?, ?)",
        ).use { statement ->
            credits.forEach { row ->
                statement.setInt(1, row.getValue("annee").toInt())
                statement.setObject(2, programIds.getValue(row.getValue("ligne_source")))
                statement.setString(3, CATEGORIES.getValue(row.getValue("nature_economique")))
                // la source est en milliers de FCFA
                statement.setBigDecimal(4, BigDecimal(row.getValue("montant_milliers_fcfa")).multiply(THOUSAND).setScale(0, RoundingMode.HALF_UP))
                statement.setString(5, SOURCE)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun validity(rows: List<Map<String, String>>): Pair<Int, Int?> {
        val from = rows.mapNotNull { it.getValue("premiere_annee_credits").toIntOrNull() }.minOrNull() ?: FIRST_SOURCE_YEAR
        val to = rows.mapNotNull { it.getValue("derniere_annee_credits").toIntOrNull() }.maxOrNull()
        return from to (if (to == null || to >= LAST_SOURCE_YEAR) null else to)
    }

    private fun organizationKey(row: Map<String, String>) = row.getValue("type") + "|" + row.getValue("institution_libelle")

    private fun organizationName(row: Map<String, String>) = row.getValue("institution_libelle")

    private fun organizationType(row: Map<String, String>): String = when {
        row.getValue("type") != "DOTATION" -> "MINISTRY"
        isCommonAllocation(row) -> "BUDGET_ALLOCATION"
        else -> "INSTITUTION"
    }

    private fun isCommonAllocation(row: Map<String, String>): Boolean {
        val label = row.getValue("libelle").lowercase()
        return listOf("dépenses", "crédits", "charges").any { label.contains(it) }
    }

    private fun organizationCode(row: Map<String, String>): String {
        val sigle = row.getValue("institution_sigle")
        if (sigle.isNotBlank()) return sigle
        val name = row.getValue("institution_libelle").lowercase()
        return INSTITUTION_CODES.entries.firstOrNull { (fragment, _) -> name.contains(fragment) }?.value
            ?: "DOT-${row.getValue("code")}"
    }

    private fun readCsv(path: String): List<Map<String, String>> {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: error("Ressource introuvable : $path")
        return stream.reader(StandardCharsets.UTF_8).use { Csv.readRecords(it) }
    }

    private companion object {
        const val SOURCE = "DGB — Tableaux de classifications croisées 2022-2028, budget LF 2026"
        const val FIRST_SOURCE_YEAR = 2022
        const val LAST_SOURCE_YEAR = 2028
        val THOUSAND = BigDecimal(1000)

        val CATEGORIES = mapOf(
            "T2_PERSONNEL" to "PERSONNEL",
            "T3_BIENS_SERVICES" to "GOODS_AND_SERVICES",
            "T4_TRANSFERTS" to "TRANSFERS",
            "T5_INVESTISSEMENT_RESSOURCES_INTERIEURES" to "CAPITAL_DOMESTIC",
            "T5_INVESTISSEMENT_RESSOURCES_EXTERIEURES" to "CAPITAL_EXTERNAL",
            "T5_INVESTISSEMENT_DONS" to "CAPITAL_EXTERNAL_GRANTS",
            "T5_INVESTISSEMENT_PRETS" to "CAPITAL_EXTERNAL_LOANS",
        )

        /** Codes usuels des institutions dotées (fragment du libellé -> code). */
        val INSTITUTION_CODES = linkedMapOf(
            "assemblée nationale" to "AN",
            "cour constitutionnelle" to "CC",
            "cour suprême" to "CS",
            "conseil economique et social" to "CES",
            "haute autorité de l’audiovisuel" to "HAAC",
            "haute cour de justice" to "HCJ",
            "médiateur de la république" to "MR",
            "commission electorale nationale autonome" to "CENA",
            "cour des comptes" to "CDC",
            "commission béninoise des droits de l'homme" to "CBDH",
            "présidence de la république" to "PR",
            "autorité de protection des données" to "APDP",
        )
    }
}
