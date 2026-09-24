-- Crédits pluriannuels par programme et catégorie économique, tels que publiés
-- (classifications croisées LF 2026, DGB). Sert d'historique et de base aux enveloppes.

CREATE TABLE budget.program_allocation (
    fiscal_year  INTEGER NOT NULL,
    program_id   UUID    NOT NULL REFERENCES referential.program (id),
    category     TEXT    NOT NULL,
    amount       NUMERIC(18, 0) NOT NULL,
    source       TEXT    NOT NULL,
    PRIMARY KEY (fiscal_year, program_id, category)
);
