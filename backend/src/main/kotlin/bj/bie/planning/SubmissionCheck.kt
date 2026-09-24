package bj.bie.planning

import bj.bie.budget.EnvelopeCheckLine
import bj.bie.costing.ControlStatus
import bj.bie.costing.CostingLine
import bj.bie.shared.RuleViolation
import java.time.LocalDate

data class SubmissionReport(
    val blocking: List<RuleViolation>,
    val warnings: List<RuleViolation>,
) {
    val submittable: Boolean get() = blocking.isEmpty()
}

/**
 * Contrôles déterministes avant soumission d'un PTAB (Rules First, AI Second).
 * Bloquant : ce qui rendrait le PTAB faux ou irrégulier. Avertissement : ce qui doit être
 * traité avant l'exécution mais n'empêche pas l'examen (ex. homologation d'un article).
 */
object SubmissionCheck {

    fun run(
        fiscalYear: Int,
        activities: List<OperationalActivity>,
        tasks: List<Task>,
        lines: List<CostingLine>,
        envelopeCheck: List<EnvelopeCheckLine>,
    ): SubmissionReport {
        val blocking = mutableListOf<RuleViolation>()
        val warnings = mutableListOf<RuleViolation>()
        val yearStart = LocalDate.of(fiscalYear, 1, 1)
        val yearEnd = LocalDate.of(fiscalYear, 12, 31)
        val tasksByActivity = tasks.groupBy { it.activityId }
        val linesByTask = lines.groupBy { it.taskId }
        val tasksById = tasks.associateBy { it.id }

        if (activities.isEmpty()) {
            blocking += RuleViolation("PTAB_VIDE", "Le PTAB ne contient aucune activité.")
        }
        for (activity in activities) {
            val activityTasks = tasksByActivity[activity.id].orEmpty()
            if (activity.startDate < yearStart || activity.endDate > yearEnd) {
                blocking += violation("ACTIVITE_HORS_EXERCICE", "L'activité ${activity.code} déborde de l'exercice $fiscalYear.", activity)
            }
            if (activityTasks.isEmpty()) {
                blocking += violation("ACTIVITE_SANS_TACHE", "L'activité ${activity.code} n'a aucune tâche.", activity)
                continue
            }
            if (activityTasks.none { linesByTask[it.id].orEmpty().isNotEmpty() }) {
                blocking += violation("ACTIVITE_SANS_COSTING", "L'activité ${activity.code} n'est pas chiffrée (aucune ligne de coût).", activity)
            }
            for (task in activityTasks) {
                if (task.startDate < activity.startDate || task.endDate > activity.endDate) {
                    blocking += RuleViolation(
                        "TACHE_HORS_PERIODE_ACTIVITE",
                        "La tâche « ${task.label} » sort de la période de l'activité ${activity.code}.",
                        "Task", task.id.toString(),
                    )
                }
                val predecessor = task.dependsOnTaskId?.let(tasksById::get)
                if (predecessor != null && predecessor.endDate > task.startDate) {
                    warnings += RuleViolation(
                        "DEPENDANCE_A_RISQUE",
                        "La tâche « ${task.label} » commence avant la fin de la tâche dont elle dépend (« ${predecessor.label} »).",
                        "Task", task.id.toString(),
                    )
                }
            }
        }
        for (line in lines) {
            if (line.controlStatus == ControlStatus.BLOCKING) {
                blocking += RuleViolation(
                    "LIGNE_DE_COUT_BLOQUANTE",
                    "Ligne « ${line.description} » : " + line.controlFindings.filter { it.level.name == "BLOCKING" }.joinToString(" ") { it.message },
                    "CostingLine", line.id.toString(),
                )
            }
            if (line.homologationRequired) {
                warnings += RuleViolation(
                    "HOMOLOGATION_REQUISE",
                    "Ligne « ${line.description} » : article hors répertoire, homologation CERPR à obtenir avant la passation.",
                    "CostingLine", line.id.toString(),
                )
            }
        }
        for (check in envelopeCheck.filter { it.overrun }) {
            val reason = if (check.hasEnvelope) {
                "besoin ${check.need} FCFA pour un disponible de ${check.currentCeiling - check.reserved} FCFA"
            } else {
                "aucune enveloppe ${check.category} ouverte pour ce programme"
            }
            blocking += RuleViolation(
                "DEPASSEMENT_ENVELOPPE",
                "Programme ${check.programCode} (${check.category}) : $reason.",
                "Program", check.programId.toString(),
            )
        }
        return SubmissionReport(blocking, warnings)
    }

    private fun violation(code: String, message: String, activity: OperationalActivity) =
        RuleViolation(code, message, "OperationalActivity", activity.id.toString())
}
