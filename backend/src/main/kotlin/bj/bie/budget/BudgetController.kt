package bj.bie.budget

import bj.bie.referential.ReferentialRepository
import bj.bie.shared.caller
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

data class ReviseCeilingRequest(
    @field:NotNull @field:DecimalMin("0") val newCeiling: BigDecimal?,
    @field:NotBlank val justification: String?,
)

@RestController
@RequestMapping("/api/v1/budget/envelopes")
class BudgetController(private val budget: BudgetService, private val referential: ReferentialRepository) {

    @GetMapping
    fun envelopes(request: HttpServletRequest, @RequestParam fiscalYear: Int): List<Envelope> =
        budget.envelopes(request.caller(referential), fiscalYear)

    @PutMapping("/{id}/ceiling")
    fun reviseCeiling(request: HttpServletRequest, @PathVariable id: UUID, @Valid @RequestBody body: ReviseCeilingRequest): Envelope =
        budget.reviseCeiling(request.caller(referential), id, body.newCeiling!!, body.justification!!)
}
