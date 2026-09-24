package bj.bie.shared

/** Règle métier non respectée, identifiée par un code stable et expliquée en français. */
data class RuleViolation(
    val code: String,
    val message: String,
    val objectType: String? = null,
    val objectId: String? = null,
)

open class BusinessException(message: String) : RuntimeException(message)

/** L'objet demandé n'existe pas, ou n'appartient pas à l'institution de l'utilisateur. */
class NotFoundException(message: String) : BusinessException(message)

/** La requête est incomplète ou incohérente. */
class InvalidRequestException(message: String) : BusinessException(message)

/** L'opération viole une ou plusieurs règles métier (ex. : soumission d'un PTAB incomplet). */
class RuleViolationException(message: String, val violations: List<RuleViolation>) : BusinessException(message)

/** L'opération viole la séparation des fonctions (préparer ≠ contrôler ≠ approuver). */
class SeparationOfDutiesException(message: String) : BusinessException(message)

/** L'opération est interdite dans l'état actuel de l'objet (ex. : modifier un PTAB soumis). */
class InvalidStateException(message: String) : BusinessException(message)
