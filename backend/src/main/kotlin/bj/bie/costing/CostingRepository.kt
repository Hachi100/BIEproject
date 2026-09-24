package bj.bie.costing

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID

data class CostingLine(
    val id: UUID,
    val taskId: UUID,
    val description: String,
    val resourceNature: ResourceNature,
    val quantity: BigDecimal,
    val unit: String,
    val frequency: BigDecimal,
    val unitPrice: BigDecimal,
    val total: BigDecimal,
    val priceSource: PriceSource,
    val priceReferenceCode: String?,
    val economicTitle: EconomicTitle,
    val economicTitleOrigin: String,
    val fundingSource: FundingSource,
    val justification: String?,
    val controlStatus: ControlStatus,
    val controlFindings: List<Finding>,
    val homologationRequired: Boolean,
)

@Repository
class CostingRepository(private val jdbc: JdbcClient, private val json: JsonMapper) {

    /** Recherche plein texte dans le répertoire des prix (dernière édition chargée). */
    fun searchPriceReferences(query: String, limit: Int): List<PriceReference> =
        jdbc.sql(
            """
            SELECT * FROM costing.price_reference
            WHERE search_text @@ websearch_to_tsquery('french', :q) OR code = :q
            ORDER BY ts_rank(search_text, websearch_to_tsquery('french', :q)) DESC, designation
            LIMIT :limit
            """,
        ).param("q", query).param("limit", limit).query { rs, _ -> rs.toPriceReference() }.list()

    fun findPriceReference(code: String): PriceReference? =
        jdbc.sql("SELECT * FROM costing.price_reference WHERE code = :code ORDER BY edition_date DESC LIMIT 1")
            .param("code", code).query { rs, _ -> rs.toPriceReference() }.optional().orElse(null)

    fun countPriceReferences(): Long =
        jdbc.sql("SELECT count(*) FROM costing.price_reference").query(Long::class.java).single()

    fun insert(tenantId: UUID, line: CostingLine, priceReferenceId: UUID?) {
        jdbc.sql(
            """
            INSERT INTO costing.costing_line
                (id, tenant_id, task_id, description, resource_nature, quantity, unit, frequency, unit_price, total,
                 price_source, price_reference_id, economic_title, economic_title_origin, funding_source, justification,
                 control_status, control_findings, homologation_required)
            VALUES (:id, :tenant, :task, :description, :nature, :quantity, :unit, :frequency, :unitPrice, :total,
                    :source, :reference, :title, :titleOrigin, :funding, :justification,
                    :status, CAST(:findings AS JSONB), :homologation)
            """,
        ).param("id", line.id).param("tenant", tenantId).param("task", line.taskId).param("description", line.description)
            .param("nature", line.resourceNature.name).param("quantity", line.quantity).param("unit", line.unit)
            .param("frequency", line.frequency).param("unitPrice", line.unitPrice).param("total", line.total)
            .param("source", line.priceSource.name).param("reference", priceReferenceId)
            .param("title", line.economicTitle.name).param("titleOrigin", line.economicTitleOrigin)
            .param("funding", line.fundingSource.name).param("justification", line.justification)
            .param("status", line.controlStatus.name).param("findings", json.writeValueAsString(line.controlFindings))
            .param("homologation", line.homologationRequired).update()
    }

    fun findLine(tenantId: UUID, id: UUID): CostingLine? =
        jdbc.sql(
            """
            SELECT l.*, r.code AS reference_code FROM costing.costing_line l
            LEFT JOIN costing.price_reference r ON r.id = l.price_reference_id
            WHERE l.id = :id AND l.tenant_id = :tenant
            """,
        ).param("id", id).param("tenant", tenantId).query { rs, _ -> rs.toCostingLine() }.optional().orElse(null)

    fun delete(tenantId: UUID, id: UUID) {
        jdbc.sql("DELETE FROM costing.costing_line WHERE id = :id AND tenant_id = :tenant").param("id", id).param("tenant", tenantId).update()
    }

    fun linesOfPtab(tenantId: UUID, ptabId: UUID): List<CostingLine> =
        jdbc.sql(
            """
            SELECT l.*, r.code AS reference_code
            FROM costing.costing_line l
            JOIN planning.task t ON t.id = l.task_id
            JOIN planning.operational_activity a ON a.id = t.activity_id
            LEFT JOIN costing.price_reference r ON r.id = l.price_reference_id
            WHERE a.ptab_id = :ptab AND l.tenant_id = :tenant
            ORDER BY a.code, t.sequence, l.created_at
            """,
        ).param("ptab", ptabId).param("tenant", tenantId).query { rs, _ -> rs.toCostingLine() }.list()

    private fun ResultSet.toPriceReference() = PriceReference(
        id = getObject("id", UUID::class.java),
        code = getString("code"),
        family = getString("family"),
        designation = getString("designation"),
        specifications = getString("specifications"),
        unit = getString("unit"),
        lowerBound = getBigDecimal("lower_bound"),
        upperBound = getBigDecimal("upper_bound"),
        edition = getString("edition"),
        editionDate = getObject("edition_date", LocalDate::class.java),
        sourcePage = getObject("source_page") as Int?,
        qualityFlags = (getArray("quality_flags").array as Array<*>).map { it.toString() },
    )

    private fun ResultSet.toCostingLine() = CostingLine(
        id = getObject("id", UUID::class.java),
        taskId = getObject("task_id", UUID::class.java),
        description = getString("description"),
        resourceNature = ResourceNature.valueOf(getString("resource_nature")),
        quantity = getBigDecimal("quantity").stripTrailingZeros(),
        unit = getString("unit"),
        frequency = getBigDecimal("frequency").stripTrailingZeros(),
        unitPrice = getBigDecimal("unit_price"),
        total = getBigDecimal("total"),
        priceSource = PriceSource.valueOf(getString("price_source")),
        priceReferenceCode = getString("reference_code"),
        economicTitle = EconomicTitle.valueOf(getString("economic_title")),
        economicTitleOrigin = getString("economic_title_origin"),
        fundingSource = FundingSource.valueOf(getString("funding_source")),
        justification = getString("justification"),
        controlStatus = ControlStatus.valueOf(getString("control_status")),
        controlFindings = json.readValue(getString("control_findings"), object : TypeReference<List<Finding>>() {}),
        homologationRequired = getBoolean("homologation_required"),
    )
}
