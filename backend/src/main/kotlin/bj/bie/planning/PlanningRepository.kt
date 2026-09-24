package bj.bie.planning

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

/** Accès aux données du PTAB. Toutes les lectures sont filtrées par institution (tenant). */
@Repository
class PlanningRepository(private val jdbc: JdbcClient) {

    fun insertPtab(ptab: Ptab) {
        jdbc.sql(
            """
            INSERT INTO planning.ptab (id, tenant_id, fiscal_year, version, status, created_by)
            VALUES (:id, :tenant, :year, :version, :status, :createdBy)
            """,
        ).param("id", ptab.id).param("tenant", ptab.tenantId).param("year", ptab.fiscalYear)
            .param("version", ptab.version).param("status", ptab.status.name).param("createdBy", ptab.createdBy)
            .update()
    }

    fun nextVersion(tenantId: UUID, fiscalYear: Int): Int =
        jdbc.sql("SELECT coalesce(max(version), 0) + 1 FROM planning.ptab WHERE tenant_id = :tenant AND fiscal_year = :year")
            .param("tenant", tenantId).param("year", fiscalYear).query(Int::class.java).single()

    fun findPtab(tenantId: UUID, id: UUID): Ptab? =
        jdbc.sql("SELECT * FROM planning.ptab WHERE id = :id AND tenant_id = :tenant")
            .param("id", id).param("tenant", tenantId).query { rs, _ -> rs.toPtab() }.optional().orElse(null)

    /** Verrouille la ligne du PTAB pour sérialiser les transitions concurrentes. */
    fun lockPtab(tenantId: UUID, id: UUID): Ptab? =
        jdbc.sql("SELECT * FROM planning.ptab WHERE id = :id AND tenant_id = :tenant FOR UPDATE")
            .param("id", id).param("tenant", tenantId).query { rs, _ -> rs.toPtab() }.optional().orElse(null)

    fun listPtabs(tenantId: UUID, fiscalYear: Int?): List<Ptab> =
        jdbc.sql(
            """
            SELECT * FROM planning.ptab
            WHERE tenant_id = :tenant AND (CAST(:year AS INTEGER) IS NULL OR fiscal_year = :year)
            ORDER BY fiscal_year DESC, version DESC
            """,
        ).param("tenant", tenantId).param("year", fiscalYear).query { rs, _ -> rs.toPtab() }.list()

    fun updateStatus(ptab: Ptab) {
        jdbc.sql(
            """
            UPDATE planning.ptab SET status = :status, submitted_by = :submittedBy, reviewed_by = :reviewedBy,
                   validated_by = :validatedBy, approved_by = :approvedBy
            WHERE id = :id AND tenant_id = :tenant
            """,
        ).param("status", ptab.status.name).param("submittedBy", ptab.actors.submittedBy)
            .param("reviewedBy", ptab.actors.reviewedBy).param("validatedBy", ptab.actors.validatedBy)
            .param("approvedBy", ptab.approvedBy).param("id", ptab.id).param("tenant", ptab.tenantId).update()
    }

    fun insertActivity(tenantId: UUID, activity: OperationalActivity) {
        jdbc.sql(
            """
            INSERT INTO planning.operational_activity
                (id, tenant_id, ptab_id, code, label, program_id, responsible_unit, expected_result,
                 start_date, end_date, execution_mode, location)
            VALUES (:id, :tenant, :ptab, :code, :label, :program, :unit, :result, :start, :end, :mode, :location)
            """,
        ).param("id", activity.id).param("tenant", tenantId).param("ptab", activity.ptabId).param("code", activity.code)
            .param("label", activity.label).param("program", activity.programId).param("unit", activity.responsibleUnit)
            .param("result", activity.expectedResult).param("start", activity.startDate).param("end", activity.endDate)
            .param("mode", activity.executionMode.name).param("location", activity.location).update()
    }

    fun countActivities(ptabId: UUID): Int =
        jdbc.sql("SELECT count(*) FROM planning.operational_activity WHERE ptab_id = :ptab")
            .param("ptab", ptabId).query(Int::class.java).single()

    fun activityCodeExists(ptabId: UUID, code: String): Boolean =
        jdbc.sql("SELECT count(*) FROM planning.operational_activity WHERE ptab_id = :ptab AND code = :code")
            .param("ptab", ptabId).param("code", code).query(Int::class.java).single() > 0

    fun activities(tenantId: UUID, ptabId: UUID): List<OperationalActivity> =
        jdbc.sql("$ACTIVITY_SELECT WHERE a.ptab_id = :ptab AND a.tenant_id = :tenant ORDER BY a.code")
            .param("ptab", ptabId).param("tenant", tenantId).query { rs, _ -> rs.toActivity() }.list()

    fun findActivity(tenantId: UUID, id: UUID): OperationalActivity? =
        jdbc.sql("$ACTIVITY_SELECT WHERE a.id = :id AND a.tenant_id = :tenant")
            .param("id", id).param("tenant", tenantId).query { rs, _ -> rs.toActivity() }.optional().orElse(null)

    fun insertTask(tenantId: UUID, task: Task) {
        jdbc.sql(
            """
            INSERT INTO planning.task (id, tenant_id, activity_id, sequence, label, responsible, start_date, end_date, depends_on_task_id)
            VALUES (:id, :tenant, :activity, :sequence, :label, :responsible, :start, :end, :dependsOn)
            """,
        ).param("id", task.id).param("tenant", tenantId).param("activity", task.activityId).param("sequence", task.sequence)
            .param("label", task.label).param("responsible", task.responsible).param("start", task.startDate)
            .param("end", task.endDate).param("dependsOn", task.dependsOnTaskId).update()
    }

    fun nextTaskSequence(activityId: UUID): Int =
        jdbc.sql("SELECT coalesce(max(sequence), 0) + 1 FROM planning.task WHERE activity_id = :activity")
            .param("activity", activityId).query(Int::class.java).single()

    fun tasks(tenantId: UUID, ptabId: UUID): List<Task> =
        jdbc.sql(
            """
            SELECT t.* FROM planning.task t JOIN planning.operational_activity a ON a.id = t.activity_id
            WHERE a.ptab_id = :ptab AND t.tenant_id = :tenant ORDER BY a.code, t.sequence
            """,
        ).param("ptab", ptabId).param("tenant", tenantId).query { rs, _ -> rs.toTask() }.list()

    fun findTask(tenantId: UUID, id: UUID): Task? =
        jdbc.sql("SELECT * FROM planning.task WHERE id = :id AND tenant_id = :tenant")
            .param("id", id).param("tenant", tenantId).query { rs, _ -> rs.toTask() }.optional().orElse(null)

    fun taskContext(tenantId: UUID, taskId: UUID): TaskContext? =
        jdbc.sql(
            """
            SELECT t.id AS task_id, a.id AS activity_id, p.id AS ptab_id, p.tenant_id, p.fiscal_year, a.program_id, p.status
            FROM planning.task t
            JOIN planning.operational_activity a ON a.id = t.activity_id
            JOIN planning.ptab p ON p.id = a.ptab_id
            WHERE t.id = :task AND t.tenant_id = :tenant
            """,
        ).param("task", taskId).param("tenant", tenantId).query { rs, _ ->
            TaskContext(
                taskId = rs.getObject("task_id", UUID::class.java),
                activityId = rs.getObject("activity_id", UUID::class.java),
                ptabId = rs.getObject("ptab_id", UUID::class.java),
                tenantId = rs.getObject("tenant_id", UUID::class.java),
                fiscalYear = rs.getInt("fiscal_year"),
                programId = rs.getObject("program_id", UUID::class.java),
                ptabStatus = PtabStatus.valueOf(rs.getString("status")),
            )
        }.optional().orElse(null)

    private fun ResultSet.toPtab() = Ptab(
        id = getObject("id", UUID::class.java),
        tenantId = getObject("tenant_id", UUID::class.java),
        fiscalYear = getInt("fiscal_year"),
        version = getInt("version"),
        status = PtabStatus.valueOf(getString("status")),
        createdBy = getString("created_by"),
        actors = PtabActors(getString("submitted_by"), getString("reviewed_by"), getString("validated_by")),
        approvedBy = getString("approved_by"),
    )

    private fun ResultSet.toActivity() = OperationalActivity(
        id = getObject("id", UUID::class.java),
        ptabId = getObject("ptab_id", UUID::class.java),
        code = getString("code"),
        label = getString("label"),
        programId = getObject("program_id", UUID::class.java),
        programCode = getString("program_code"),
        responsibleUnit = getString("responsible_unit"),
        expectedResult = getString("expected_result"),
        startDate = getObject("start_date", java.time.LocalDate::class.java),
        endDate = getObject("end_date", java.time.LocalDate::class.java),
        executionMode = ExecutionMode.valueOf(getString("execution_mode")),
        location = getString("location"),
    )

    private fun ResultSet.toTask() = Task(
        id = getObject("id", UUID::class.java),
        activityId = getObject("activity_id", UUID::class.java),
        sequence = getInt("sequence"),
        label = getString("label"),
        responsible = getString("responsible"),
        startDate = getObject("start_date", java.time.LocalDate::class.java),
        endDate = getObject("end_date", java.time.LocalDate::class.java),
        dependsOnTaskId = getObject("depends_on_task_id", UUID::class.java),
    )

    private companion object {
        const val ACTIVITY_SELECT = """
            SELECT a.*, p.code AS program_code
            FROM planning.operational_activity a JOIN referential.program p ON p.id = a.program_id
        """
    }
}
