package bj.bie.referential

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Référentiels nationaux, consultables sans institution active. */
@RestController
@RequestMapping("/api/v1/referential")
class ReferentialController(private val referential: ReferentialRepository) {

    @GetMapping("/organizations")
    fun organizations(@RequestParam(required = false) year: Int?): List<Organization> = referential.organizations(year)

    @GetMapping("/programs")
    fun programs(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) organization: String?,
    ): List<Program> = referential.programs(year, organization)
}
