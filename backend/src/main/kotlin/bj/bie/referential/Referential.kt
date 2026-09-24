package bj.bie.referential

import bj.bie.shared.TenantResolver
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

enum class OrganizationType { MINISTRY, INSTITUTION, BUDGET_ALLOCATION }

enum class ProgramType { PROGRAM, BUDGET_ALLOCATION }

data class Organization(
    val id: UUID,
    val code: String,
    val name: String,
    val type: OrganizationType,
    val validFromYear: Int,
    val validToYear: Int?,
) {
    fun isActiveIn(year: Int) = year >= validFromYear && (validToYear == null || year <= validToYear)
}

data class Program(
    val id: UUID,
    val code: String,
    val label: String,
    val type: ProgramType,
    val organizationId: UUID,
    val organizationCode: String,
    val validFromYear: Int,
    val validToYear: Int?,
    val qualityFlags: List<String>,
) {
    fun isActiveIn(year: Int) = year >= validFromYear && (validToYear == null || year <= validToYear)
}

@Repository
class ReferentialRepository(private val jdbc: JdbcClient) : TenantResolver {

    fun organizations(year: Int?): List<Organization> =
        jdbc.sql(
            """
            SELECT id, code, name, type, valid_from_year, valid_to_year FROM referential.organization
            WHERE CAST(:year AS INTEGER) IS NULL
               OR (valid_from_year <= :year AND (valid_to_year IS NULL OR valid_to_year >= :year))
            ORDER BY type, code
            """,
        ).param("year", year).query { rs, _ -> rs.toOrganization() }.list()

    override fun resolve(code: String): UUID? =
        jdbc.sql(
            """
            SELECT id FROM referential.organization
            WHERE upper(code) = upper(:code) AND valid_to_year IS NULL
            ORDER BY valid_from_year DESC LIMIT 1
            """,
        ).param("code", code).query(UUID::class.java).optional().orElse(null)

    fun programs(year: Int?, organizationCode: String?): List<Program> =
        jdbc.sql(
            """
            $PROGRAM_SELECT
            WHERE (CAST(:year AS INTEGER) IS NULL
                   OR (p.valid_from_year <= :year AND (p.valid_to_year IS NULL OR p.valid_to_year >= :year)))
              AND (CAST(:org AS TEXT) IS NULL OR upper(o.code) = upper(:org))
            ORDER BY o.code, p.code
            """,
        ).param("year", year).param("org", organizationCode).query { rs, _ -> rs.toProgram() }.list()

    /** Programme portant ce code et actif l'année donnée pour l'institution (le code seul n'est pas unique). */
    fun activeProgram(organizationId: UUID, code: String, year: Int): Program? =
        jdbc.sql(
            """
            $PROGRAM_SELECT
            WHERE p.organization_id = :org AND p.code = :code
              AND p.valid_from_year <= :year AND (p.valid_to_year IS NULL OR p.valid_to_year >= :year)
            """,
        ).param("org", organizationId).param("code", code).param("year", year)
            .query { rs, _ -> rs.toProgram() }.optional().orElse(null)

    fun program(id: UUID): Program? =
        jdbc.sql("$PROGRAM_SELECT WHERE p.id = :id").param("id", id).query { rs, _ -> rs.toProgram() }.optional().orElse(null)

    fun fiscalYearExists(year: Int): Boolean =
        jdbc.sql("SELECT count(*) FROM referential.fiscal_year WHERE year = :year").param("year", year)
            .query(Long::class.java).single() > 0

    private fun ResultSet.toOrganization() = Organization(
        id = getObject("id", UUID::class.java),
        code = getString("code"),
        name = getString("name"),
        type = OrganizationType.valueOf(getString("type")),
        validFromYear = getInt("valid_from_year"),
        validToYear = getObject("valid_to_year") as Int?,
    )

    private fun ResultSet.toProgram() = Program(
        id = getObject("id", UUID::class.java),
        code = getString("code"),
        label = getString("label"),
        type = ProgramType.valueOf(getString("type")),
        organizationId = getObject("organization_id", UUID::class.java),
        organizationCode = getString("organization_code"),
        validFromYear = getInt("valid_from_year"),
        validToYear = getObject("valid_to_year") as Int?,
        qualityFlags = (getArray("quality_flags").array as Array<*>).map { it.toString() },
    )

    private companion object {
        const val PROGRAM_SELECT = """
            SELECT p.id, p.code, p.label, p.type, p.organization_id, o.code AS organization_code,
                   p.valid_from_year, p.valid_to_year, p.quality_flags
            FROM referential.program p JOIN referential.organization o ON o.id = p.organization_id
        """
    }
}
