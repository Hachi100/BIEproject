-- PTAB, costing et enveloppes (Parties 3 et 4 du cahier des charges).
-- Toute donnée d'une institution porte un tenant_id (organisation propriétaire).

CREATE SCHEMA IF NOT EXISTS planning;
CREATE SCHEMA IF NOT EXISTS costing;
CREATE SCHEMA IF NOT EXISTS budget;

-- Répertoire des prix de référence (e-Répertoire, DNCF) : une ligne par article et par édition.
CREATE TABLE costing.price_reference (
    id              UUID PRIMARY KEY,
    code            TEXT    NOT NULL,
    family          TEXT    NOT NULL,
    designation     TEXT    NOT NULL,
    specifications  TEXT,
    unit            TEXT,
    lower_bound     NUMERIC(18, 2),
    upper_bound     NUMERIC(18, 2),
    edition         TEXT    NOT NULL,
    edition_date    DATE    NOT NULL,
    source_page     INTEGER,
    quality_flags   TEXT[]  NOT NULL DEFAULT '{}',
    search_text     TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('french', coalesce(designation, '') || ' ' || coalesce(family, '') || ' ' || coalesce(specifications, ''))
    ) STORED,
    UNIQUE (code, edition)
);
CREATE INDEX price_reference_search_idx ON costing.price_reference USING GIN (search_text);

CREATE TABLE planning.ptab (
    id              UUID PRIMARY KEY,
    tenant_id       UUID    NOT NULL REFERENCES referential.organization (id),
    fiscal_year     INTEGER NOT NULL REFERENCES referential.fiscal_year (year),
    version         INTEGER NOT NULL DEFAULT 1,
    status          TEXT    NOT NULL,
    supersedes_id   UUID REFERENCES planning.ptab (id),
    created_by      TEXT    NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    submitted_by    TEXT,
    reviewed_by     TEXT,
    validated_by    TEXT,
    approved_by     TEXT,
    UNIQUE (tenant_id, fiscal_year, version)
);

CREATE TABLE planning.operational_activity (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID    NOT NULL REFERENCES referential.organization (id),
    ptab_id            UUID    NOT NULL REFERENCES planning.ptab (id) ON DELETE CASCADE,
    code               TEXT    NOT NULL,
    label              TEXT    NOT NULL,
    program_id         UUID    NOT NULL REFERENCES referential.program (id),
    responsible_unit   TEXT    NOT NULL,
    expected_result    TEXT,
    start_date         DATE    NOT NULL,
    end_date           DATE    NOT NULL,
    execution_mode     TEXT    NOT NULL CHECK (execution_mode IN ('DIRECT', 'INDIRECT', 'MIXED')),
    location           TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (ptab_id, code),
    CHECK (end_date >= start_date)
);
CREATE INDEX operational_activity_tenant_idx ON planning.operational_activity (tenant_id);

CREATE TABLE planning.task (
    id                   UUID PRIMARY KEY,
    tenant_id            UUID    NOT NULL REFERENCES referential.organization (id),
    activity_id          UUID    NOT NULL REFERENCES planning.operational_activity (id) ON DELETE CASCADE,
    sequence             INTEGER NOT NULL,
    label                TEXT    NOT NULL,
    responsible          TEXT    NOT NULL,
    start_date           DATE    NOT NULL,
    end_date             DATE    NOT NULL,
    depends_on_task_id   UUID REFERENCES planning.task (id),
    UNIQUE (activity_id, sequence),
    CHECK (end_date >= start_date)
);

CREATE TABLE costing.costing_line (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID    NOT NULL REFERENCES referential.organization (id),
    task_id             UUID    NOT NULL REFERENCES planning.task (id) ON DELETE CASCADE,
    description         TEXT    NOT NULL,
    resource_nature     TEXT    NOT NULL,
    quantity            NUMERIC(18, 4) NOT NULL CHECK (quantity > 0),
    unit                TEXT    NOT NULL,
    frequency           NUMERIC(18, 4) NOT NULL DEFAULT 1 CHECK (frequency > 0),
    unit_price          NUMERIC(18, 2) NOT NULL CHECK (unit_price >= 0),
    total               NUMERIC(18, 0) NOT NULL,
    price_source        TEXT    NOT NULL,
    price_reference_id  UUID REFERENCES costing.price_reference (id),
    economic_title      TEXT    NOT NULL,
    economic_title_origin TEXT  NOT NULL CHECK (economic_title_origin IN ('DECLARED', 'INFERRED_FROM_PRICE_REFERENCE')),
    funding_source      TEXT    NOT NULL,
    justification       TEXT,
    control_status      TEXT    NOT NULL CHECK (control_status IN ('COMPLIANT', 'WARNING', 'BLOCKING')),
    control_findings    JSONB   NOT NULL DEFAULT '[]',
    homologation_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX costing_line_task_idx ON costing.costing_line (task_id);

-- Enveloppes budgétaires : plafond par exercice × programme × catégorie économique.
CREATE TABLE budget.envelope (
    id                UUID PRIMARY KEY,
    tenant_id         UUID    NOT NULL REFERENCES referential.organization (id),
    fiscal_year       INTEGER NOT NULL REFERENCES referential.fiscal_year (year),
    program_id        UUID    NOT NULL REFERENCES referential.program (id),
    category          TEXT    NOT NULL,
    initial_ceiling   NUMERIC(18, 0) NOT NULL CHECK (initial_ceiling >= 0),
    current_ceiling   NUMERIC(18, 0) NOT NULL CHECK (current_ceiling >= 0),
    reserved          NUMERIC(18, 0) NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    version           INTEGER NOT NULL DEFAULT 1,
    source            TEXT    NOT NULL,
    UNIQUE (fiscal_year, program_id, category)
);
CREATE INDEX envelope_tenant_year_idx ON budget.envelope (tenant_id, fiscal_year);

-- Une révision de plafond ne remplace jamais silencieusement la valeur précédente.
CREATE TABLE budget.envelope_revision (
    id              UUID PRIMARY KEY,
    envelope_id     UUID    NOT NULL REFERENCES budget.envelope (id),
    version         INTEGER NOT NULL,
    previous_ceiling NUMERIC(18, 0) NOT NULL,
    new_ceiling     NUMERIC(18, 0) NOT NULL,
    justification   TEXT    NOT NULL,
    revised_by      TEXT    NOT NULL,
    revised_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (envelope_id, version)
);
