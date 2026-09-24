package bj.bie.planning

import bj.bie.shared.InvalidRequestException
import bj.bie.shared.InvalidStateException
import bj.bie.shared.SeparationOfDutiesException

/** États génériques d'un instrument officiel (Partie 2 §22), appliqués au PTAB. */
enum class PtabStatus(val editable: Boolean) {
    DRAFT(true),
    SUBMITTED(false),
    UNDER_REVIEW(false),
    RETURNED(true),
    VALIDATED(false),
    APPROVED(false),
}

enum class PtabAction { SUBMIT, START_REVIEW, RETURN, VALIDATE, APPROVE }

/** Acteurs déjà intervenus sur le PTAB, pour appliquer la séparation des fonctions. */
data class PtabActors(
    val submittedBy: String? = null,
    val reviewedBy: String? = null,
    val validatedBy: String? = null,
)

/**
 * Machine à états du PTAB avec séparation des fonctions (Partie 2 §23, Partie 9 §11) :
 * PRÉPARER ≠ CONTRÔLER ≠ APPROUVER. Le workflow n'est jamais codé sur une personne
 * nommée : il compare les acteurs successifs.
 */
object PtabWorkflow {

    fun next(current: PtabStatus, action: PtabAction, actor: String, actors: PtabActors, comment: String?): PtabStatus {
        val target = when (action) {
            PtabAction.SUBMIT -> transition(current, action, PtabStatus.SUBMITTED, PtabStatus.DRAFT, PtabStatus.RETURNED)
            PtabAction.START_REVIEW -> transition(current, action, PtabStatus.UNDER_REVIEW, PtabStatus.SUBMITTED)
            PtabAction.RETURN -> transition(current, action, PtabStatus.RETURNED, PtabStatus.UNDER_REVIEW)
            PtabAction.VALIDATE -> transition(current, action, PtabStatus.VALIDATED, PtabStatus.UNDER_REVIEW)
            PtabAction.APPROVE -> transition(current, action, PtabStatus.APPROVED, PtabStatus.VALIDATED)
        }
        when (action) {
            PtabAction.START_REVIEW, PtabAction.VALIDATE, PtabAction.RETURN ->
                if (actor == actors.submittedBy) {
                    throw SeparationOfDutiesException("La personne qui a préparé et soumis le PTAB ($actor) ne peut pas le contrôler.")
                }
            PtabAction.APPROVE ->
                if (actor == actors.submittedBy || actor == actors.validatedBy) {
                    throw SeparationOfDutiesException("L'approbation doit être faite par une personne différente du préparateur et du contrôleur.")
                }
            PtabAction.SUBMIT -> Unit
        }
        if (action == PtabAction.RETURN && comment.isNullOrBlank()) {
            throw InvalidRequestException("Un renvoi pour correction doit être motivé par un commentaire.")
        }
        return target
    }

    private fun transition(current: PtabStatus, action: PtabAction, target: PtabStatus, vararg allowedFrom: PtabStatus): PtabStatus {
        if (current !in allowedFrom) {
            throw InvalidStateException("Action $action impossible : le PTAB est à l'état $current (attendu : ${allowedFrom.joinToString(" ou ")}).")
        }
        return target
    }
}
