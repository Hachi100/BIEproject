-- Journal d'audit inaltérable (Partie 9, §31-32).
-- Chaque événement est chaîné au précédent de la même chaîne (une chaîne par institution,
-- plus une chaîne NATIONAL) par une empreinte SHA-256 calculée par PostgreSQL.
-- Toute modification ou suppression est refusée au niveau de la base.

CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE audit.audit_event (
    id             BIGSERIAL PRIMARY KEY,
    chain_key      TEXT        NOT NULL,
    occurred_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    tenant_id      UUID,
    actor          TEXT        NOT NULL,
    action         TEXT        NOT NULL,
    object_type    TEXT        NOT NULL,
    object_id      TEXT        NOT NULL,
    before_state   JSONB,
    after_state    JSONB,
    justification  TEXT,
    previous_hash  CHAR(64)    NOT NULL,
    hash           CHAR(64)    NOT NULL
);
CREATE INDEX audit_event_object_idx ON audit.audit_event (object_type, object_id);
CREATE INDEX audit_event_chain_idx ON audit.audit_event (chain_key, id);

-- Représentation canonique d'un événement, utilisée à l'écriture et à la vérification.
CREATE FUNCTION audit.event_digest(
    p_previous_hash TEXT, p_chain_key TEXT, p_occurred_at TIMESTAMPTZ, p_tenant_id UUID, p_actor TEXT,
    p_action TEXT, p_object_type TEXT, p_object_id TEXT, p_before JSONB, p_after JSONB, p_justification TEXT
) RETURNS CHAR(64) LANGUAGE SQL IMMUTABLE AS $$
    SELECT encode(sha256(convert_to(concat_ws('|',
        p_previous_hash,
        p_chain_key,
        to_char(p_occurred_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),
        coalesce(p_tenant_id::text, ''),
        p_actor, p_action, p_object_type, p_object_id,
        coalesce(p_before::text, ''),
        coalesce(p_after::text, ''),
        coalesce(p_justification, '')
    ), 'UTF8')), 'hex')
$$;

CREATE FUNCTION audit.reject_mutation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Le journal d''audit est inaltérable : % refusé', TG_OP USING ERRCODE = 'insufficient_privilege';
END;
$$;

CREATE TRIGGER audit_event_immutable
    BEFORE UPDATE OR DELETE ON audit.audit_event
    FOR EACH ROW EXECUTE FUNCTION audit.reject_mutation();

CREATE TRIGGER audit_event_no_truncate
    BEFORE TRUNCATE ON audit.audit_event
    FOR EACH STATEMENT EXECUTE FUNCTION audit.reject_mutation();
