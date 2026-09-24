package bj.bie.audit

import bj.bie.referential.ReferentialRepository
import bj.bie.shared.caller
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/audit")
class AuditController(private val audit: AuditJournal, private val referential: ReferentialRepository) {

    @GetMapping("/events")
    fun events(
        request: HttpServletRequest,
        @RequestParam(required = false) objectId: String?,
        @RequestParam(defaultValue = "100") limit: Int,
    ): List<AuditEvent> = audit.events(request.caller(referential).tenantId, objectId, limit.coerceIn(1, 1000))

    /** Vérifie l'intégrité de la chaîne d'audit de l'institution. */
    @GetMapping("/verification")
    fun verify(request: HttpServletRequest): ChainVerification = audit.verify(request.caller(referential).tenantId.toString())
}
