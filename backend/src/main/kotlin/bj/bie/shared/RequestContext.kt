package bj.bie.shared

import jakarta.servlet.http.HttpServletRequest
import java.util.UUID

/**
 * Identité de l'appelant pour une requête : utilisateur et institution (tenant).
 *
 * Incrément 1 : l'identité est transmise par les en-têtes `X-BIE-User` et `X-BIE-Tenant`
 * (code de l'institution, ex. « MS »). Elle sera fournie par Keycloak (jeton OIDC) à
 * l'incrément suivant ; les services métier ne dépendent que de cette abstraction.
 */
data class Caller(
    val userId: String,
    val tenantId: UUID,
    val tenantCode: String,
)

const val USER_HEADER = "X-BIE-User"
const val TENANT_HEADER = "X-BIE-Tenant"

fun interface TenantResolver {
    /** Retourne l'identifiant de l'institution active portant ce code, ou null. */
    fun resolve(code: String): UUID?
}

fun HttpServletRequest.caller(tenantResolver: TenantResolver): Caller {
    val user = getHeader(USER_HEADER)?.trim().orEmpty()
    val tenantCode = getHeader(TENANT_HEADER)?.trim()?.uppercase().orEmpty()
    if (user.isEmpty()) throw InvalidRequestException("En-tête $USER_HEADER manquant : l'utilisateur doit être identifié.")
    if (tenantCode.isEmpty()) throw InvalidRequestException("En-tête $TENANT_HEADER manquant : l'institution doit être précisée.")
    val tenantId = tenantResolver.resolve(tenantCode)
        ?: throw InvalidRequestException("Institution inconnue ou inactive : $tenantCode.")
    return Caller(userId = user, tenantId = tenantId, tenantCode = tenantCode)
}
