package bj.bie.planning

import bj.bie.budget.EnvelopeCheckLine
import bj.bie.costing.CostingLine
import bj.bie.costing.CostingService
import bj.bie.costing.EconomicTitle
import bj.bie.costing.FundingSource
import bj.bie.costing.NewCostingLine
import bj.bie.costing.PriceSource
import bj.bie.costing.ResourceNature
import bj.bie.referential.ReferentialRepository
import bj.bie.shared.caller
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CreatePtabRequest(@field:NotNull val fiscalYear: Int?)

data class CreateActivityRequest(
    val code: String? = null,
    @field:NotBlank val label: String?,
    @field:NotBlank val programCode: String?,
    @field:NotBlank val responsibleUnit: String?,
    val expectedResult: String? = null,
    @field:NotNull val startDate: LocalDate?,
    @field:NotNull val endDate: LocalDate?,
    @field:NotNull val executionMode: ExecutionMode?,
    val location: String? = null,
)

data class CreateTaskRequest(
    @field:NotBlank val label: String?,
    @field:NotBlank val responsible: String?,
    @field:NotNull val startDate: LocalDate?,
    @field:NotNull val endDate: LocalDate?,
    val dependsOnTaskId: UUID? = null,
)

data class CreateCostingLineRequest(
    @field:NotBlank val description: String?,
    @field:NotNull val resourceNature: ResourceNature?,
    @field:NotNull @field:DecimalMin(value = "0", inclusive = false) val quantity: BigDecimal?,
    @field:NotBlank val unit: String?,
    @field:DecimalMin(value = "0", inclusive = false) val frequency: BigDecimal? = null,
    @field:NotNull @field:DecimalMin("0") val unitPrice: BigDecimal?,
    @field:NotNull val priceSource: PriceSource?,
    val priceReferenceCode: String? = null,
    val economicTitle: EconomicTitle? = null,
    val fundingSource: FundingSource? = null,
    val justification: String? = null,
)

data class TransitionRequest(@field:NotNull val action: PtabAction?, val comment: String? = null)

@RestController
@RequestMapping("/api/v1")
class PlanningController(
    private val planning: PlanningService,
    private val costing: CostingService,
    private val referential: ReferentialRepository,
) {

    @PostMapping("/ptabs")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPtab(request: HttpServletRequest, @Valid @RequestBody body: CreatePtabRequest): Ptab =
        planning.createPtab(request.caller(referential), body.fiscalYear!!)

    @GetMapping("/ptabs")
    fun ptabs(request: HttpServletRequest, @RequestParam(required = false) fiscalYear: Int?): List<Ptab> =
        planning.ptabs(request.caller(referential), fiscalYear)

    @GetMapping("/ptabs/{id}")
    fun ptab(request: HttpServletRequest, @PathVariable id: UUID): PtabView = planning.ptab(request.caller(referential), id)

    @PostMapping("/ptabs/{id}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    fun addActivity(request: HttpServletRequest, @PathVariable id: UUID, @Valid @RequestBody body: CreateActivityRequest): OperationalActivity =
        planning.addActivity(
            request.caller(referential),
            id,
            NewActivity(
                code = body.code,
                label = body.label!!,
                programCode = body.programCode!!,
                responsibleUnit = body.responsibleUnit!!,
                expectedResult = body.expectedResult,
                startDate = body.startDate!!,
                endDate = body.endDate!!,
                executionMode = body.executionMode!!,
                location = body.location,
            ),
        )

    @PostMapping("/activities/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTask(request: HttpServletRequest, @PathVariable id: UUID, @Valid @RequestBody body: CreateTaskRequest): Task =
        planning.addTask(
            request.caller(referential),
            id,
            NewTask(body.label!!, body.responsible!!, body.startDate!!, body.endDate!!, body.dependsOnTaskId),
        )

    @PostMapping("/tasks/{id}/costing-lines")
    @ResponseStatus(HttpStatus.CREATED)
    fun addCostingLine(request: HttpServletRequest, @PathVariable id: UUID, @Valid @RequestBody body: CreateCostingLineRequest): CostingLine =
        costing.addLine(
            request.caller(referential),
            id,
            NewCostingLine(
                description = body.description!!,
                resourceNature = body.resourceNature!!,
                quantity = body.quantity!!,
                unit = body.unit!!,
                frequency = body.frequency ?: BigDecimal.ONE,
                unitPrice = body.unitPrice!!,
                priceSource = body.priceSource!!,
                priceReferenceCode = body.priceReferenceCode,
                economicTitle = body.economicTitle,
                fundingSource = body.fundingSource ?: FundingSource.NATIONAL_BUDGET,
                justification = body.justification,
            ),
        )

    @DeleteMapping("/costing-lines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCostingLine(request: HttpServletRequest, @PathVariable id: UUID, @RequestParam(required = false) justification: String?) =
        costing.deleteLine(request.caller(referential), id, justification)

    @GetMapping("/ptabs/{id}/envelope-check")
    fun envelopeCheck(request: HttpServletRequest, @PathVariable id: UUID): List<EnvelopeCheckLine> =
        planning.envelopeCheck(request.caller(referential), id)

    @GetMapping("/ptabs/{id}/submission-check")
    fun submissionCheck(request: HttpServletRequest, @PathVariable id: UUID): SubmissionReport =
        planning.submissionCheck(request.caller(referential), id)

    @PostMapping("/ptabs/{id}/transitions")
    fun transition(request: HttpServletRequest, @PathVariable id: UUID, @Valid @RequestBody body: TransitionRequest): Ptab =
        planning.transition(request.caller(referential), id, body.action!!, body.comment)
}
