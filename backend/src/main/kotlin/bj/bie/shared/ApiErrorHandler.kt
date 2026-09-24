package bj.bie.shared

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Traduit les erreurs métier en réponses RFC 9457 (Problem Details) compréhensibles. */
@RestControllerAdvice
class ApiErrorHandler {

    @ExceptionHandler(NotFoundException::class)
    fun notFound(e: NotFoundException) = problem(HttpStatus.NOT_FOUND, "Objet introuvable", e.message)

    @ExceptionHandler(InvalidRequestException::class)
    fun invalid(e: InvalidRequestException) = problem(HttpStatus.BAD_REQUEST, "Requête invalide", e.message)

    @ExceptionHandler(InvalidStateException::class)
    fun invalidState(e: InvalidStateException) = problem(HttpStatus.CONFLICT, "Opération impossible dans l'état actuel", e.message)

    @ExceptionHandler(SeparationOfDutiesException::class)
    fun separationOfDuties(e: SeparationOfDutiesException) =
        problem(HttpStatus.FORBIDDEN, "Séparation des fonctions", e.message)

    @ExceptionHandler(RuleViolationException::class)
    fun ruleViolation(e: RuleViolationException) =
        problem(HttpStatus.UNPROCESSABLE_CONTENT, "Règles métier non respectées", e.message).apply {
            setProperty("violations", e.violations)
        }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(e: MethodArgumentNotValidException) =
        problem(HttpStatus.BAD_REQUEST, "Requête invalide", "Certains champs sont invalides.").apply {
            setProperty(
                "violations",
                e.bindingResult.fieldErrors.map { RuleViolation(code = "CHAMP_INVALIDE", message = "${it.field} : ${it.defaultMessage}") },
            )
        }

    private fun problem(status: HttpStatus, title: String, detail: String?): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail ?: title).apply { this.title = title }
}
