package bj.bie.planning

import bj.bie.audit.AuditJournal
import bj.bie.budget.BudgetService
import bj.bie.budget.EnvelopeCheckLine
import bj.bie.costing.ControlStatus
import bj.bie.costing.CostingLine
import bj.bie.costing.CostingRepository
import bj.bie.referential.ReferentialRepository
import bj.bie.shared.Caller
import bj.bie.shared.InvalidRequestException
import bj.bie.shared.InvalidStateException
import bj.bie.shared.NotFoundException
import bj.bie.shared.RuleViolationException
import bj.bie.shared.sumFcfa
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class NewActivity(
    val code: String? = null,
    val label: String,
    val programCode: String,
    val responsibleUnit: String,
    val expectedResult: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val executionMode: ExecutionMode,
    val location: String? = null,
)

data class NewTask(
    val label: String,
    val responsible: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dependsOnTaskId: UUID? = null,
)

data class TaskView(val task: Task, val costingLines: List<CostingLine>, val total: BigDecimal)

data class ActivityView(val activity: OperationalActivity, val tasks: List<TaskView>, val total: BigDecimal)

data class PtabView(
    val ptab: Ptab,
    val activities: List<ActivityView>,
    val total: BigDecimal,
    val blockingLines: Int,
    val linesRequiringHomologation: Int,
)

@Service
class PlanningService(
    private val planning: PlanningRepository,
    private val referential: ReferentialRepository,
    private val costing: CostingRepository,
    private val budget: BudgetService,
    private val audit: AuditJournal,
) {

    @Transactional
    fun createPtab(caller: Caller, fiscalYear: Int): Ptab {
        if (!referential.fiscalYearExists(fiscalYear)) throw InvalidRequestException("Exercice $fiscalYear non ouvert dans le référentiel.")
        val ptab = Ptab(
            id = UUID.randomUUID(),
            tenantId = caller.tenantId,
            fiscalYear = fiscalYear,
            version = planning.nextVersion(caller.tenantId, fiscalYear),
            status = PtabStatus.DRAFT,
            createdBy = caller.userId,
            actors = PtabActors(),
            approvedBy = null,
        )
        planning.insertPtab(ptab)
        audit.record(caller.tenantId, caller.userId, "PTAB_CREATED", "Ptab", ptab.id, after = ptab)
        return ptab
    }

    fun ptabs(caller: Caller, fiscalYear: Int?): List<Ptab> = planning.listPtabs(caller.tenantId, fiscalYear)

    fun ptab(caller: Caller, id: UUID): PtabView {
        val ptab = findPtab(caller, id)
        val lines = costing.linesOfPtab(caller.tenantId, id)
        val linesByTask = lines.groupBy { it.taskId }
        val tasksByActivity = planning.tasks(caller.tenantId, id).groupBy { it.activityId }
        val activities = planning.activities(caller.tenantId, id).map { activity ->
            val tasks = tasksByActivity[activity.id].orEmpty().map { task ->
                val taskLines = linesByTask[task.id].orEmpty()
                TaskView(task, taskLines, taskLines.map { it.total }.sumFcfa())
            }
            ActivityView(activity, tasks, tasks.map { it.total }.sumFcfa())
        }
        return PtabView(
            ptab = ptab,
            activities = activities,
            total = activities.map { it.total }.sumFcfa(),
            blockingLines = lines.count { it.controlStatus == ControlStatus.BLOCKING },
            linesRequiringHomologation = lines.count { it.homologationRequired },
        )
    }

    @Transactional
    fun addActivity(caller: Caller, ptabId: UUID, command: NewActivity): OperationalActivity {
        val ptab = editablePtab(caller, ptabId)
        if (command.label.isBlank() || command.responsibleUnit.isBlank()) {
            throw InvalidRequestException("Le libellé et la structure responsable de l'activité sont obligatoires.")
        }
        if (command.endDate < command.startDate) throw InvalidRequestException("La date de fin précède la date de début.")
        val program = referential.activeProgram(caller.tenantId, command.programCode.trim(), ptab.fiscalYear)
            ?: throw InvalidRequestException(
                "Programme ${command.programCode} inconnu pour ${caller.tenantCode} ou inactif en ${ptab.fiscalYear}.",
            )
        val code = command.code?.trim()?.takeIf { it.isNotEmpty() }
            ?: "${program.code}-${(planning.countActivities(ptabId) + 1).toString().padStart(3, '0')}"
        if (planning.activityCodeExists(ptabId, code)) throw InvalidRequestException("Le code d'activité $code existe déjà dans ce PTAB.")
        val activity = OperationalActivity(
            id = UUID.randomUUID(),
            ptabId = ptabId,
            code = code,
            label = command.label.trim(),
            programId = program.id,
            programCode = program.code,
            responsibleUnit = command.responsibleUnit.trim(),
            expectedResult = command.expectedResult?.trim(),
            startDate = command.startDate,
            endDate = command.endDate,
            executionMode = command.executionMode,
            location = command.location?.trim(),
        )
        planning.insertActivity(caller.tenantId, activity)
        audit.record(caller.tenantId, caller.userId, "ACTIVITY_ADDED", "OperationalActivity", activity.id, after = activity)
        return activity
    }

    @Transactional
    fun addTask(caller: Caller, activityId: UUID, command: NewTask): Task {
        val activity = planning.findActivity(caller.tenantId, activityId)
            ?: throw NotFoundException("Activité $activityId introuvable pour l'institution ${caller.tenantCode}.")
        editablePtab(caller, activity.ptabId)
        if (command.label.isBlank() || command.responsible.isBlank()) {
            throw InvalidRequestException("Le libellé et le responsable de la tâche sont obligatoires.")
        }
        if (command.endDate < command.startDate) throw InvalidRequestException("La date de fin précède la date de début.")
        command.dependsOnTaskId?.let { dependency ->
            val predecessor = planning.taskContext(caller.tenantId, dependency)
            if (predecessor == null || predecessor.ptabId != activity.ptabId) {
                throw InvalidRequestException("La tâche prédécesseur doit appartenir au même PTAB.")
            }
        }
        val task = Task(
            id = UUID.randomUUID(),
            activityId = activityId,
            sequence = planning.nextTaskSequence(activityId),
            label = command.label.trim(),
            responsible = command.responsible.trim(),
            startDate = command.startDate,
            endDate = command.endDate,
            dependsOnTaskId = command.dependsOnTaskId,
        )
        planning.insertTask(caller.tenantId, task)
        audit.record(caller.tenantId, caller.userId, "TASK_ADDED", "Task", task.id, after = task)
        return task
    }

    fun envelopeCheck(caller: Caller, ptabId: UUID): List<EnvelopeCheckLine> {
        val ptab = findPtab(caller, ptabId)
        return envelopeCheck(ptab)
    }

    fun submissionCheck(caller: Caller, ptabId: UUID): SubmissionReport = submissionCheck(findPtab(caller, ptabId))

    @Transactional
    fun transition(caller: Caller, ptabId: UUID, action: PtabAction, comment: String?): Ptab {
        val ptab = planning.lockPtab(caller.tenantId, ptabId)
            ?: throw NotFoundException("PTAB $ptabId introuvable pour l'institution ${caller.tenantCode}.")
        val target = PtabWorkflow.next(ptab.status, action, caller.userId, ptab.actors, comment)
        if (action == PtabAction.SUBMIT) {
            val report = submissionCheck(ptab)
            if (!report.submittable) {
                throw RuleViolationException("Le PTAB ne peut pas être soumis : ${report.blocking.size} règle(s) non respectée(s).", report.blocking)
            }
        }
        val actors = when (action) {
            PtabAction.SUBMIT -> ptab.actors.copy(submittedBy = caller.userId, reviewedBy = null, validatedBy = null)
            PtabAction.START_REVIEW, PtabAction.RETURN -> ptab.actors.copy(reviewedBy = caller.userId)
            PtabAction.VALIDATE -> ptab.actors.copy(reviewedBy = caller.userId, validatedBy = caller.userId)
            PtabAction.APPROVE -> ptab.actors
        }
        val updated = ptab.copy(
            status = target,
            actors = actors,
            approvedBy = if (action == PtabAction.APPROVE) caller.userId else ptab.approvedBy,
        )
        planning.updateStatus(updated)
        audit.record(
            tenantId = caller.tenantId,
            actor = caller.userId,
            action = "PTAB_$action",
            objectType = "Ptab",
            objectId = ptab.id,
            before = mapOf("status" to ptab.status),
            after = mapOf("status" to updated.status),
            justification = comment,
        )
        return updated
    }

    private fun submissionCheck(ptab: Ptab): SubmissionReport =
        SubmissionCheck.run(
            fiscalYear = ptab.fiscalYear,
            activities = planning.activities(ptab.tenantId, ptab.id),
            tasks = planning.tasks(ptab.tenantId, ptab.id),
            lines = costing.linesOfPtab(ptab.tenantId, ptab.id),
            envelopeCheck = envelopeCheck(ptab),
        )

    private fun envelopeCheck(ptab: Ptab): List<EnvelopeCheckLine> {
        val labels = planning.activities(ptab.tenantId, ptab.id)
            .map { it.programId }.distinct()
            .mapNotNull { referential.program(it) }
            .associate { it.id to (it.code to it.label) }
        return budget.checkPtab(ptab.tenantId, ptab.id, ptab.fiscalYear, labels)
    }

    private fun findPtab(caller: Caller, id: UUID): Ptab =
        planning.findPtab(caller.tenantId, id) ?: throw NotFoundException("PTAB $id introuvable pour l'institution ${caller.tenantCode}.")

    private fun editablePtab(caller: Caller, id: UUID): Ptab {
        val ptab = findPtab(caller, id)
        if (!ptab.status.editable) throw InvalidStateException("Le PTAB est à l'état ${ptab.status} : il ne peut plus être modifié.")
        return ptab
    }
}
