package bj.bie.planning

import java.time.LocalDate
import java.util.UUID

/** Mode d'exécution : pilote notamment la génération du PPM (Partie 3 §28-31). */
enum class ExecutionMode { DIRECT, INDIRECT, MIXED }

data class Ptab(
    val id: UUID,
    val tenantId: UUID,
    val fiscalYear: Int,
    val version: Int,
    val status: PtabStatus,
    val createdBy: String,
    val actors: PtabActors,
    val approvedBy: String?,
)

data class OperationalActivity(
    val id: UUID,
    val ptabId: UUID,
    val code: String,
    val label: String,
    val programId: UUID,
    val programCode: String,
    val responsibleUnit: String,
    val expectedResult: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val executionMode: ExecutionMode,
    val location: String?,
)

data class Task(
    val id: UUID,
    val activityId: UUID,
    val sequence: Int,
    val label: String,
    val responsible: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dependsOnTaskId: UUID?,
)

/** Contexte d'une tâche utile aux autres modules (costing, budget). */
data class TaskContext(
    val taskId: UUID,
    val activityId: UUID,
    val ptabId: UUID,
    val tenantId: UUID,
    val fiscalYear: Int,
    val programId: UUID,
    val ptabStatus: PtabStatus,
)
