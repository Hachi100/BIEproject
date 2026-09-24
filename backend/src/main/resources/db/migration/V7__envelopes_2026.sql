-- Enveloppes 2026 : plafonds de la loi de finances 2026 par programme et catégorie économique.
-- Dérivées des crédits publiés (Data Once) : aucune ressaisie.

INSERT INTO budget.envelope (id, tenant_id, fiscal_year, program_id, category, initial_ceiling, current_ceiling, source)
SELECT gen_random_uuid(), p.organization_id, a.fiscal_year, a.program_id, a.category, a.amount, a.amount,
       'LF 2026 — ' || a.source
FROM budget.program_allocation a
JOIN referential.program p ON p.id = a.program_id
WHERE a.fiscal_year = 2026 AND a.amount > 0;
