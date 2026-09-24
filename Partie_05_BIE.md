---
title: "Partie 5 — Procurement Intelligence Engine, PPM, contrats et investissements publics"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 5 — Procurement Intelligence Engine, PPM, contrats et investissements publics

## De la programmation des besoins à l'exécution contractuelle et au portefeuille d'investissements

## 1. Objet

La Partie 5 définit le **Procurement Intelligence Engine** et le **Public Investment Engine**.

Chaîne cible :

```text
PTAB
 ↓
TASKS
 ↓
RESOURCES
 ↓
PROCUREMENT NEEDS
 ↓
AGGREGATION
 ↓
PROCUREMENT STRATEGY
 ↓
PPM
 ↓
PROCEDURE
 ↓
CONTRACT
 ↓
EXECUTION
 ↓
RECEPTION
 ↓
PAYMENT
```

BIE prépare, contrôle, simule et suit ; il ne devient pas un système parallèle de passation électronique.

## 2. Principe : le PPM découle du PTAB

```text
PTAB
 ↓
Execution mode
 ↓
External resources
 ↓
Procurement needs
 ↓
PPM
```

La ressaisie manuelle doit être supprimée lorsque les données existent déjà.

## 3. ProcurementNeed

```text
id
fiscal_year
organization
source_activity
source_task
resource
description
category
quantity
unit
estimated_amount
funding_source
budget_line
needed_by_date
status
```

## 4. Catégories

```text
WORKS
SUPPLIES
SERVICES
INTELLECTUAL_SERVICES_FIRM
INTELLECTUAL_SERVICES_INDIVIDUAL
OTHER
```

## 5. Classification

L'IA peut proposer une catégorie, mais les règles de procédure reposent sur des données structurées et un Rules Engine.

## 6. Procurement Rules Engine

```text
NEED
 ↓
CATEGORY
 ↓
AMOUNT
 ↓
AUTHORITY TYPE
 ↓
FUNDING REGIME
 ↓
THRESHOLD REGISTRY
 ↓
PROPOSED PROCEDURE
```

## 7. Seuils versionnés

Les seuils ne sont jamais codés en dur.

Objet :

`ProcurementThresholdRule`

```text
jurisdiction
authority_type
category
procedure
lower_bound
upper_bound
control_authority
valid_from
valid_to
legal_source
```

## 8. Anti-Splitting Engine

Détecter les besoins potentiellement fractionnés selon :

- nature ;
- période ;
- structure ;
- spécification ;
- marché fournisseur.

Le système émet une alerte, jamais une accusation automatique.

## 9. Aggregation Engine

Le moteur peut regrouper :

```text
Need A
Need B
Need C
→ Common procurement
```

Dimensions :

- catégorie ;
- spécification ;
- période ;
- territoire ;
- financement ;
- points de livraison.

## 10. Mutualisation

À maturité, BIE peut détecter des besoins communs à plusieurs institutions.

## 11. Procurement Strategy

Objet :

```text
buying_objective
market_structure
aggregation
lotting
procedure
calendar
risks
contract_strategy
```

## 12. Supplier Market Intelligence

Lorsque les données sont disponibles :

- concurrence historique ;
- nombre d'offres ;
- délais ;
- échecs ;
- niveaux de prix.

## 13. Allotment Engine

BIE peut comparer plusieurs scénarios d'allotissement mais la décision reste humaine.

## 14. ProcurementPlanItem

```text
designation
category
source_needs[]
estimated_amount
funding_sources[]
procedure
review_type
launch_date
expected_award_date
expected_signature_date
expected_completion_date
status
```

## 15. Génération PPM

```text
Generate PPM Draft
```

Pipeline :

1. détecter besoins ;
2. regrouper ;
3. calculer montants ;
4. appliquer règles ;
5. proposer calendrier ;
6. créer brouillon.

## 16. Statuts PPM

```text
AI_DRAFT
WORKING_DRAFT
INTERNAL_REVIEW
SUBMITTED_FOR_CONTROL
APPROVED
PUBLISHED
IN_EXECUTION
REVISED
CLOSED
```

## 17. Révision

Le système calcule un diff :

```text
+ new items
- removed
± amount changed
± calendar changed
± procedure changed
```

## 18. Reverse Procurement Planning

À partir de `needed_by_date`, BIE calcule la date limite sûre de lancement.

```text
Needed date
 ↑
Delivery
 ↑
Contract
 ↑
Award
 ↑
Evaluation
 ↑
Launch
 ↑
Preparation
```

## 19. Readiness Score

Dimensions possibles :

- financement ;
- spécifications ;
- études ;
- site ;
- TDR/DAO ;
- validation interne.

## 20. Procedure Recommendation

Sortie :

```text
procedure
legal basis
required controls
required documents
```

Le LLM explique ; le Rules Engine fait autorité.

## 21. Régime de financement

```text
NATIONAL
WORLD_BANK
AFDB
AFD
EU
OTHER_DONOR
SPECIAL_REGIME
```

Les règles bailleurs et nationales restent distinctes.

## 22. Document Engine

BIE gère des modèles versionnés de :

- DAO ;
- TDR ;
- DRP ;
- demandes de cotation ;
- rapports ;
- contrats.

Les clauses officielles ne doivent pas être librement réinventées par un LLM.

## 23. Clause Types

```text
LOCKED_OFFICIAL
CONTROLLED
FREE
```

## 24. TDR / Specification Intelligence

L'IA aide à rédiger :

- contexte ;
- objectifs ;
- livrables ;
- spécifications ;
- critères d'acceptation.

## 25. Procurement Workspace

```text
Overview
Planning
Documents
Workflow
Actors
Dates
Contract
Execution
Risks
Audit
```

## 26. Procurement Timeline

Vue Gantt/timeline des étapes réglementaires et opérationnelles.

## 27. Early Warning

Exemple :

> Marché devant être lancé dans 20 jours mais DAO non prêt.

## 28. Failed Procurement

```text
FAILED
 ↓
ROOT CAUSE
 ↓
RELAUNCH
 ↓
RECALCULATE IMPACT
```

## 29. Contract

Objet :

```text
contract_number
supplier
award_amount
signature_date
start_date
end_date
funding_source
status
```

## 30. Estimated vs Contract

BIE compare :

```text
estimate
contract amount
actual payments
```

## 31. Milestones

```text
deliverable
planned_date
amount
acceptance_condition
status
```

## 32. PCC Link

Le calendrier de paiement contractuel met à jour le PCC.

## 33. AE/CP Link

Les contrats pluriannuels alimentent la programmation AE/CP.

## 34. Amendments

Chaque avenant conserve :

```text
reason
amount_change
duration_change
scope_change
approval
```

et recalcule l'impact.

## 35. Guarantees

Suivi :

- garantie de soumission ;
- bonne exécution ;
- avance ;
- retenue.

## 36. Deliverables / Reception

Workflow :

```text
DELIVERED
 ↓
REVIEW
 ↓
ACCEPTED / RETURNED
```

## 37. Audit Pack

BIE peut constituer un dossier :

```text
PPM
Need
Costing
DAO
Publications
Evaluation
Award
Contract
Guarantees
Reception
Payments
```

## 38. Compliance Engine

Contrôles :

- PPM approuvé ;
- procédure cohérente ;
- contrôle réalisé ;
- documents disponibles ;
- délais ;
- approbation.

## 39. E-procurement Gateway

```text
BIE Canonical Model
 ↓
Adapter
 ↓
Official procurement system
```

Les données déjà disponibles dans le système officiel doivent être synchronisées, pas ressaisies.

## 40. Public Investment Engine

Un projet d'investissement est distinct d'un marché.

```text
PROJECT
 ├── Studies
 ├── Works
 ├── Supervision
 └── Equipment
```

## 41. Investment Lifecycle

```text
IDEA
 ↓
SCREENING
 ↓
FEASIBILITY
 ↓
APPRAISAL
 ↓
ELIGIBILITY
 ↓
PROJECT BANK
 ↓
PRIORITIZATION
 ↓
PIP
 ↓
EXECUTION
 ↓
EX-POST
```

## 42. Maturity Index

Dimensions :

```text
strategic
technical
economic
environmental
land
funding
procurement
```

## 43. Gates

```text
G0 Idea
G1 Screening
G2 Feasibility
G3 Appraisal
G4 Budget readiness
G5 Procurement readiness
G6 Execution
G7 Completion
G8 Ex-post
```

## 44. Project Bank

Statuts :

```text
Ideas
Prepared
Eligible
Funded
In PIP
In execution
Completed
```

## 45. Portfolio Optimization

BIE peut comparer des portefeuilles sous contraintes budgétaires et stratégiques.

L'optimisation doit être calculée par un solver, pas par le LLM seul.

## 46. Procurement Packaging

Un projet peut produire plusieurs packages et marchés.

## 47. Project Master Schedule

Fusionner :

```text
Studies
Procurement
Contracts
Works
Payments
```

## 48. Forecasts

- délai d'achèvement ;
- dépassement de coût ;
- charge CP future ;
- risque.

## 49. PIP

```text
PROJECT BANK
 ↓
PRIORITIZATION
 ↓
BUDGET CONSTRAINT
 ↓
PIP
```

## 50. Requirements

### PRC

| ID | Exigence |
|---|---|
| PRC-001 | détecter les besoins |
| PRC-002 | lier au PTAB |
| PRC-003 | agréger |
| PRC-004 | détecter fractionnement |
| PRC-005 | règles de seuils versionnées |
| PRC-006 | calendrier |
| PRC-007 | documents |
| PRC-008 | intégration e-procurement |

### CTR

| ID | Exigence |
|---|---|
| CTR-001 | contrats |
| CTR-002 | jalons |
| CTR-003 | paiements |
| CTR-004 | avenants |
| CTR-005 | garanties |
| CTR-006 | réception |
| CTR-007 | audit |

### INV

| ID | Exigence |
|---|---|
| INV-001 | banque de projets |
| INV-002 | maturité |
| INV-003 | appraisal |
| INV-004 | priorisation |
| INV-005 | PIP |
| INV-006 | packages |
| INV-007 | AE/CP |
| INV-008 | ex-post |

## 51. Conclusion

Le PPM devient une conséquence logique de la programmation. Le projet d'investissement devient un objet longitudinal relié à la stratégie, au budget, aux marchés, aux contrats, aux paiements et aux résultats.
