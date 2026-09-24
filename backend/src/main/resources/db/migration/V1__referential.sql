-- Référentiels nationaux (Partie 2 du cahier des charges).
-- Les objets sont historisés par période de validité : un même code de programme peut être
-- porté successivement par deux programmes différents (ex. 078, 084 dans la LF 2026).

CREATE SCHEMA IF NOT EXISTS referential;

CREATE TABLE referential.fiscal_year (
    year          INTEGER PRIMARY KEY,
    status        TEXT    NOT NULL CHECK (status IN ('PREPARATION', 'OPEN', 'EXECUTION', 'CLOSING', 'CLOSED', 'ARCHIVED')),
    opening_date  DATE    NOT NULL,
    closing_date  DATE    NOT NULL,
    CHECK (closing_date > opening_date)
);

CREATE TABLE referential.organization (
    id               UUID PRIMARY KEY,
    code             TEXT NOT NULL,
    name             TEXT NOT NULL,
    type             TEXT NOT NULL CHECK (type IN ('MINISTRY', 'INSTITUTION', 'BUDGET_ALLOCATION')),
    parent_id        UUID REFERENCES referential.organization (id),
    valid_from_year  INTEGER NOT NULL,
    valid_to_year    INTEGER,
    source           TEXT NOT NULL,
    CHECK (valid_to_year IS NULL OR valid_to_year >= valid_from_year)
);
CREATE UNIQUE INDEX organization_code_period_uq ON referential.organization (code, valid_from_year);

CREATE TABLE referential.program (
    id               UUID PRIMARY KEY,
    code             TEXT NOT NULL CHECK (code ~ '^\d{3}$'),
    source_code      TEXT NOT NULL,
    label            TEXT NOT NULL,
    type             TEXT NOT NULL CHECK (type IN ('PROGRAM', 'BUDGET_ALLOCATION')),
    organization_id  UUID NOT NULL REFERENCES referential.organization (id),
    valid_from_year  INTEGER NOT NULL,
    valid_to_year    INTEGER,
    source           TEXT NOT NULL,
    source_row       INTEGER,
    quality_flags    TEXT[] NOT NULL DEFAULT '{}',
    CHECK (valid_to_year IS NULL OR valid_to_year >= valid_from_year)
);
CREATE INDEX program_code_idx ON referential.program (code);
CREATE INDEX program_organization_idx ON referential.program (organization_id);

CREATE TABLE referential.action (
    id          UUID PRIMARY KEY,
    program_id  UUID NOT NULL REFERENCES referential.program (id),
    code        TEXT NOT NULL,
    label       TEXT NOT NULL,
    UNIQUE (program_id, code)
);

INSERT INTO referential.fiscal_year (year, status, opening_date, closing_date) VALUES
    (2026, 'EXECUTION',   DATE '2026-01-01', DATE '2026-12-31'),
    (2027, 'PREPARATION', DATE '2027-01-01', DATE '2027-12-31');
