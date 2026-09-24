package bj.bie.costing

import bj.bie.shared.excludingVat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

data class PriceReferenceView(
    val reference: PriceReference,
    val lowerBoundExcludingVat: BigDecimal?,
    val upperBoundExcludingVat: BigDecimal?,
)

/** Consultation du répertoire des prix de référence (référentiel national). */
@RestController
@RequestMapping("/api/v1/costing/price-references")
class CostingController(private val costing: CostingService) {

    @GetMapping
    fun search(@RequestParam q: String, @RequestParam(defaultValue = "20") limit: Int): List<PriceReferenceView> =
        costing.searchPriceReferences(q, limit).map(::view)

    @GetMapping("/{code}")
    fun byCode(@PathVariable code: String): PriceReferenceView = view(costing.priceReference(code))

    private fun view(reference: PriceReference) = PriceReferenceView(
        reference = reference,
        lowerBoundExcludingVat = reference.lowerBound?.excludingVat(),
        upperBoundExcludingVat = reference.upperBound?.excludingVat(),
    )
}
