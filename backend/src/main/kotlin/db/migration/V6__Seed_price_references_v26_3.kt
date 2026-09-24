package db.migration

import bj.bie.shared.Csv
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.sql.Date
import java.sql.Types
import java.time.LocalDate
import java.util.UUID
import java.util.zip.GZIPInputStream

/**
 * Charge l'e-Répertoire des prix de référence, 19e édition v26.3 (juin 2026, DNCF),
 * extrait par scripts/referentiel/extraire_repertoire_prix.py.
 *
 * Les anomalies de la source (borne manquante, cellule « ####### » dans le PDF publié,
 * BI > BS) sont conservées dans quality_flags : le contrôle de prix les signale à
 * l'utilisateur au lieu d'utiliser une valeur douteuse.
 */
@Suppress("ClassName")
class V6__Seed_price_references_v26_3 : BaseJavaMigration() {

    override fun migrate(context: Context) {
        val stream = javaClass.classLoader.getResourceAsStream(SEED) ?: error("Ressource introuvable : $SEED")
        val rows = GZIPInputStream(stream).reader(StandardCharsets.UTF_8).use { Csv.readRecords(it) }
        context.connection.prepareStatement(
            """
            INSERT INTO costing.price_reference
                (id, code, family, designation, specifications, unit, lower_bound, upper_bound,
                 edition, edition_date, source_page, quality_flags)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
        ).use { statement ->
            rows.forEachIndexed { index, row ->
                statement.setObject(1, UUID.randomUUID())
                statement.setString(2, row.getValue("code"))
                statement.setString(3, row.getValue("famille"))
                statement.setString(4, row.getValue("designation"))
                statement.setString(5, row.getValue("specifications").ifBlank { null })
                statement.setString(6, row.getValue("unite").ifBlank { null })
                statement.setAmount(7, row.getValue("borne_inferieure"))
                statement.setAmount(8, row.getValue("borne_superieure"))
                statement.setString(9, row.getValue("edition"))
                statement.setDate(10, Date.valueOf(LocalDate.parse(row.getValue("date_edition") + "-01")))
                statement.setInt(11, row.getValue("page_source").toInt())
                val flags = row.getValue("anomalies").split('|').filter { it.isNotBlank() }.toTypedArray()
                statement.setArray(12, context.connection.createArrayOf("text", flags))
                statement.addBatch()
                if ((index + 1) % BATCH_SIZE == 0) statement.executeBatch()
            }
            statement.executeBatch()
        }
    }

    private fun java.sql.PreparedStatement.setAmount(index: Int, value: String) {
        if (value.isBlank()) setNull(index, Types.NUMERIC) else setBigDecimal(index, BigDecimal(value))
    }

    private companion object {
        const val SEED = "db/seed/repertoire_prix_v26_3.csv.gz"
        const val BATCH_SIZE = 1000
    }
}
