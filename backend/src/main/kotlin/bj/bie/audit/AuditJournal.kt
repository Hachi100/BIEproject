package bj.bie.audit

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import java.time.OffsetDateTime
import java.util.UUID

data class AuditEvent(
    val id: Long,
    val chainKey: String,
    val occurredAt: OffsetDateTime,
    val tenantId: UUID?,
    val actor: String,
    val action: String,
    val objectType: String,
    val objectId: String,
    val beforeState: String?,
    val afterState: String?,
    val justification: String?,
    val hash: String,
)

data class ChainVerification(
    val eventsChecked: Long,
    val valid: Boolean,
    val brokenEventIds: List<Long>,
)

/**
 * Journal d'audit métier inaltérable (Partie 1 §4.5, Partie 9 §31-32).
 *
 * L'événement est écrit dans la même transaction que la modification qu'il décrit :
 * pas de modification sans trace. Les écritures d'une même chaîne sont sérialisées par un
 * verrou consultatif de transaction afin que chaque empreinte référence la précédente.
 */
@Service
class AuditJournal(private val jdbc: JdbcClient, private val json: JsonMapper) {

    fun record(
        tenantId: UUID?,
        actor: String,
        action: String,
        objectType: String,
        objectId: Any,
        before: Any? = null,
        after: Any? = null,
        justification: String? = null,
    ) {
        val chainKey = tenantId?.toString() ?: NATIONAL_CHAIN
        jdbc.sql("SELECT pg_advisory_xact_lock(hashtext(:key))").param("key", "audit:$chainKey").query().singleRow()
        jdbc.sql(
            """
            INSERT INTO audit.audit_event
                (chain_key, occurred_at, tenant_id, actor, action, object_type, object_id,
                 before_state, after_state, justification, previous_hash, hash)
            SELECT :chain, s.ts, :tenant, :actor, :action, :type, :objectId,
                   CAST(:before AS JSONB), CAST(:after AS JSONB), :justification, s.prev,
                   audit.event_digest(s.prev, :chain, s.ts, :tenant, :actor, :action, :type, :objectId,
                                      CAST(:before AS JSONB), CAST(:after AS JSONB), :justification)
            FROM (
                SELECT clock_timestamp() AS ts,
                       coalesce((SELECT hash FROM audit.audit_event WHERE chain_key = :chain ORDER BY id DESC LIMIT 1),
                                repeat('0', 64)) AS prev
            ) s
            """,
        ).param("chain", chainKey).param("tenant", tenantId).param("actor", actor).param("action", action)
            .param("type", objectType).param("objectId", objectId.toString())
            .param("before", before?.let(json::writeValueAsString)).param("after", after?.let(json::writeValueAsString))
            .param("justification", justification).update()
    }

    fun events(tenantId: UUID, objectId: String?, limit: Int): List<AuditEvent> =
        jdbc.sql(
            """
            SELECT * FROM audit.audit_event
            WHERE chain_key = :chain AND (CAST(:objectId AS TEXT) IS NULL OR object_id = :objectId)
            ORDER BY id DESC LIMIT :limit
            """,
        ).param("chain", tenantId.toString()).param("objectId", objectId).param("limit", limit).query { rs, _ ->
            AuditEvent(
                id = rs.getLong("id"),
                chainKey = rs.getString("chain_key"),
                occurredAt = rs.getObject("occurred_at", OffsetDateTime::class.java),
                tenantId = rs.getObject("tenant_id", UUID::class.java),
                actor = rs.getString("actor"),
                action = rs.getString("action"),
                objectType = rs.getString("object_type"),
                objectId = rs.getString("object_id"),
                beforeState = rs.getString("before_state"),
                afterState = rs.getString("after_state"),
                justification = rs.getString("justification"),
                hash = rs.getString("hash"),
            )
        }.list()

    /** Recalcule chaque empreinte et vérifie le chaînage ; toute altération est détectée. */
    fun verify(chainKey: String): ChainVerification {
        val rows = jdbc.sql(
            """
            SELECT id,
                   hash = audit.event_digest(previous_hash, chain_key, occurred_at, tenant_id, actor, action,
                                             object_type, object_id, before_state, after_state, justification)
                   AND previous_hash = coalesce(lag(hash) OVER (ORDER BY id), repeat('0', 64)) AS intact
            FROM audit.audit_event WHERE chain_key = :chain ORDER BY id
            """,
        ).param("chain", chainKey).query { rs, _ -> rs.getLong("id") to rs.getBoolean("intact") }.list()
        val broken = rows.filterNot { it.second }.map { it.first }
        return ChainVerification(eventsChecked = rows.size.toLong(), valid = broken.isEmpty(), brokenEventIds = broken)
    }

    companion object {
        const val NATIONAL_CHAIN = "NATIONAL"
    }
}
