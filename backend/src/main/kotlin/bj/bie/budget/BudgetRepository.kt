package bj.bie.budget

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.UUID

@Repository
class BudgetRepository(private val jdbc: JdbcClient) {

    fun envelopes(tenantId: UUID, fiscalYear: Int): List<Envelope> =
        jdbc.sql("$ENVELOPE_SELECT WHERE e.tenant_id = :tenant AND e.fiscal_year = :year ORDER BY p.code, e.category")
            .param("tenant", tenantId).param("year", fiscalYear).query { rs, _ -> rs.toEnvelope() }.list()

    fun lockEnvelope(tenantId: UUID, id: UUID): Envelope? =
        jdbc.sql("$ENVELOPE_SELECT WHERE e.id = :id AND e.tenant_id = :tenant FOR UPDATE OF e")
            .param("id", id).param("tenant", tenantId).query { rs, _ -> rs.toEnvelope() }.optional().orElse(null)

    fun reviseCeiling(envelope: Envelope, newCeiling: BigDecimal, justification: String, revisedBy: String) {
        val nextVersion = envelope.version + 1
        jdbc.sql(
            """
            INSERT INTO budget.envelope_revision (id, envelope_id, version, previous_ceiling, new_ceiling, justification, revised_by)
            VALUES (:id, :envelope, :version, :previous, :new, :justification, :by)
            """,
        ).param("id", UUID.randomUUID()).param("envelope", envelope.id).param("version", nextVersion)
            .param("previous", envelope.currentCeiling).param("new", newCeiling)
            .param("justification", justification).param("by", revisedBy).update()
        jdbc.sql("UPDATE budget.envelope SET current_ceiling = :ceiling, version = :version WHERE id = :id")
            .param("ceiling", newCeiling).param("version", nextVersion).param("id", envelope.id).update()
    }

    /**
     * Besoins d'un PTAB par programme et catégorie. Lecture transverse (planning × costing)
     * assumée : c'est une vue de consolidation, sans écriture dans les autres modules.
     */
    fun needsOfPtab(tenantId: UUID, ptabId: UUID): List<Need> =
        jdbc.sql(
            """
            SELECT a.program_id, l.economic_title, l.funding_source, sum(l.total) AS amount
            FROM costing.costing_line l
            JOIN planning.task t ON t.id = l.task_id
            JOIN planning.operational_activity a ON a.id = t.activity_id
            WHERE a.ptab_id = :ptab AND l.tenant_id = :tenant
            GROUP BY a.program_id, l.economic_title, l.funding_source
            """,
        ).param("ptab", ptabId).param("tenant", tenantId).query { rs, _ ->
            Need(
                programId = rs.getObject("program_id", UUID::class.java),
                category = EnvelopeCategory.of(
                    bj.bie.costing.EconomicTitle.valueOf(rs.getString("economic_title")),
                    bj.bie.costing.FundingSource.valueOf(rs.getString("funding_source")),
                ),
                amount = rs.getBigDecimal("amount"),
            )
        }.list()

    private fun ResultSet.toEnvelope() = Envelope(
        id = getObject("id", UUID::class.java),
        tenantId = getObject("tenant_id", UUID::class.java),
        fiscalYear = getInt("fiscal_year"),
        programId = getObject("program_id", UUID::class.java),
        programCode = getString("program_code"),
        programLabel = getString("program_label"),
        category = EnvelopeCategory.valueOf(getString("category")),
        initialCeiling = getBigDecimal("initial_ceiling"),
        currentCeiling = getBigDecimal("current_ceiling"),
        reserved = getBigDecimal("reserved"),
        version = getInt("version"),
        source = getString("source"),
    )

    private companion object {
        const val ENVELOPE_SELECT = """
            SELECT e.*, p.code AS program_code, p.label AS program_label
            FROM budget.envelope e JOIN referential.program p ON p.id = e.program_id
        """
    }
}
