package bj.bie.shared

import java.io.Reader

/**
 * Lecteur CSV minimal (RFC 4180) : champs entre guillemets, guillemets doublés,
 * retours à la ligne dans les champs. Suffisant pour les fichiers de référence produits
 * par les scripts d'extraction du dépôt.
 */
object Csv {

    fun readRecords(reader: Reader): List<Map<String, String>> {
        val rows = parse(reader.readText())
        if (rows.isEmpty()) return emptyList()
        val header = rows.first()
        return rows.drop(1)
            .filter { row -> row.any { it.isNotEmpty() } }
            .map { row ->
                require(row.size == header.size) { "Ligne CSV à ${row.size} colonnes au lieu de ${header.size} : $row" }
                header.zip(row).toMap()
            }
    }

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes && c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                c == '"' -> inQuotes = !inQuotes
                !inQuotes && c == ',' -> { row.add(field.toString()); field.clear() }
                !inQuotes && (c == '\n' || c == '\r') -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(field.toString()); field.clear()
                    rows.add(row); row = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows
    }
}
