---
title: "Partie 2 — Référentiels nationaux, modèle métier canonique, structures administratives et architecture fonctionnelle"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 2 — Référentiels nationaux, modèle métier canonique, structures administratives et architecture fonctionnelle

## Le langage commun de toute la plateforme

## 1. Objet

La Partie 2 définit le **modèle métier canonique** de BIE : référentiels, hiérarchies, acteurs, relations, états, temporalité, organisation multi-tenant et domaines fonctionnels.

L'objectif est d'empêcher que chaque module recrée sa propre définition d'un programme, d'une activité, d'une structure ou d'un exercice.

## 2. Principes

- un identifiant canonique par objet ;
- versionnement temporel ;
- validité `valid_from / valid_to` ;
- audit ;
- séparation personne / fonction ;
- séparation organisation / tenant ;
- données de référence gouvernées.

## 3. Hiérarchie stratégique

```text
Vision
 └── StrategicOrientation
      └── StrategicObjective
           └── Policy
                └── GovernmentProgram
```

## 4. Hiérarchie programmatique

```text
Mission
 └── Program
      ├── ProgramObjective
      │    └── Indicator
      └── Action
           └── BudgetActivity
                └── OperationalActivity
                     └── Task
```

## 5. Hiérarchie institutionnelle

```text
État
 ├── Institution
 ├── Ministère
 │    ├── Cabinet
 │    ├── Secrétariat général
 │    ├── DPAF
 │    ├── DPPD
 │    ├── Directions techniques
 │    ├── Directions départementales
 │    └── Organismes sous tutelle
 ├── Agence
 ├── EPA
 ├── Projet / Programme
 └── Commune
```

## 6. Organization

Objet `Organization` :

```text
id
code
name
type
parent_id
legal_status
valid_from
valid_to
tenant_id
```

## 7. OrganizationUnit

Sous-structure :

```text
Direction
Service
Cellule
Département
Unité de projet
```

## 8. FiscalYear

Objet central :

```text
year
status
opening_date
closing_date
reference_calendar
```

États :

```text
PREPARATION
OPEN
EXECUTION
CLOSING
CLOSED
ARCHIVED
```

## 9. Scenario

BIE doit permettre plusieurs scénarios :

```text
BASELINE
BUDGET_CEILING
OPTIMISTIC
CONSTRAINED
CUSTOM
```

Les scénarios ne modifient jamais les données officielles avant validation.

## 10. Référentiel programmatique

Le référentiel national porte au minimum :

- mission ;
- programme ;
- objectif ;
- indicateur ;
- action ;
- activité budgétaire.

Chaque objet est versionné.

## 11. Activité opérationnelle

Objet `OperationalActivity` :

```text
id
code
label
budget_activity_id
responsible_unit
fiscal_year
start_date
end_date
execution_mode
status
```

## 12. Task

```text
id
activity_id
label
sequence
responsible
start_date
end_date
dependency
status
```

## 13. Ressource

`Resource` représente un déterminant de coût :

- personnel ;
- mission ;
- transport ;
- matériel ;
- service ;
- infrastructure ;
- consommable.

## 14. FundingSource

Exemples :

```text
National Budget
Loan
Grant
Counterpart
Special Fund
```

## 15. Classifications budgétaires

BIE devra représenter de manière distincte :

```text
Administrative
Programmatic
Economic
Functional
Geographic
Funding
```

## 16. BudgetLine

Une ligne budgétaire ne sera jamais modélisée comme un simple texte libre.

Elle sera liée à ses dimensions officielles.

## 17. Actor Model

Une personne peut occuper plusieurs fonctions dans le temps.

Objet :

`RoleAssignment`

```text
person_id
role_id
organization_id
program_id
valid_from
valid_to
```

## 18. Rôles fonctionnels

Exemples :

```text
Minister
SGM
DPAF
DPPD
RPRO
PRMP
SGFP
ResponsibleUnit
Controller
Approver
Auditor
DataSteward
Administrator
```

## 19. Séparation rôle/personne

Le workflow ne doit jamais coder :

```text
"Jean doit valider"
```

mais :

```text
"Le titulaire du rôle DPAF valide"
```

## 20. RACI

Chaque processus pourra porter :

```text
Responsible
Accountable
Consulted
Informed
```

## 21. Workflow

Objet :

`WorkflowDefinition`

et instance :

`WorkflowInstance`.

## 22. États génériques

```text
DRAFT
IN_PREPARATION
SUBMITTED
UNDER_REVIEW
RETURNED
VALIDATED
APPROVED
IN_EXECUTION
REVISED
CLOSED
ARCHIVED
```

## 23. Maker-Checker

BIE doit supporter le principe :

```text
PREPARE
≠
CHECK
≠
APPROVE
```

## 24. Versioning

Tout objet officiel important possède :

```text
version
status
valid_from
valid_to
supersedes_id
```

## 25. Révision

La révision ne remplace jamais silencieusement la version précédente.

```text
PTAB v1
→ Revision
→ PTAB v2
```

## 26. Budget Calendar

Objet national :

`BudgetCalendar`

avec :

```text
event
deadline
actor
instrument
legal_source
```

## 27. Calendrier institutionnel

Une institution peut ajouter des jalons internes sans modifier le calendrier national.

## 28. Strategic Link

Objet `StrategicAlignmentLink` :

```text
source_object
target_object
contribution_type
justification
status
```

## 29. Territorial Model

```text
Country
 └── Department
      └── Commune
           └── Arrondissement
```

Les référentiels géographiques doivent être versionnés.

## 30. PublicInvestmentProject

Projet distinct d'une activité et d'un marché.

Attributs :

```text
id
code
title
program
territory
total_cost
start_year
end_year
maturity
status
```

## 31. ProcurementNeed

Besoin issu du planning/costing.

Il doit conserver une relation vers :

```text
Task
OperationalActivity
BudgetLine
FundingSource
```

## 32. Indicator

Objet :

```text
code
label
type
definition
formula
unit
baseline
source
frequency
direction
```

## 33. IndicatorTarget

Les cibles sont séparées de la définition.

```text
indicator
year
target
scenario
```

## 34. Risk

Objet transversal :

```text
category
probability
impact
owner
mitigation
status
```

## 35. Document

Chaque document possède :

```text
type
organization
year
version
source
status
hash
```

## 36. Data Lineage métier

Exemple :

```text
Task
→ CostingLine
→ ProcurementNeed
→ PPMItem
→ Contract
→ Payment
→ Output
```

## 37. Domain Boundaries

Domains cibles :

```text
Strategy
Programmatic
Planning
Costing
Budget
Procurement
Investment
Treasury
Performance
Evaluation
Documents
Administration
```

## 38. Multi-tenant

Chaque objet métier pertinent contient un `tenant_id`.

L'isolation est appliquée :

- dans l'application ;
- dans les politiques ;
- dans la base ;
- dans les index ;
- dans l'analytics.

## 39. National vs local configuration

Hiérarchie :

```text
National
 ↓
Institution
 ↓
Program
```

Les règles nationales ne sont pas librement redéfinies localement.

## 40. Requirements

### REF

| ID | Exigence |
|---|---|
| REF-001 | gérer les référentiels nationaux |
| REF-002 | versionner les référentiels |
| REF-003 | historiser la validité |
| REF-004 | gouverner les modifications |
| REF-005 | gérer les alias |

### MOD

| ID | Exigence |
|---|---|
| MOD-001 | modèle Mission → Task |
| MOD-002 | organisation multi-niveaux |
| MOD-003 | financement |
| MOD-004 | classifications |
| MOD-005 | projets |
| MOD-006 | indicateurs |
| MOD-007 | documents |

### WF

| ID | Exigence |
|---|---|
| WF-001 | workflows configurables |
| WF-002 | maker-checker |
| WF-003 | validations |
| WF-004 | révisions |
| WF-005 | historique |
| WF-006 | deadlines |

### TEN

| ID | Exigence |
|---|---|
| TEN-001 | multi-tenant |
| TEN-002 | isolation |
| TEN-003 | configuration locale |
| TEN-004 | configuration nationale |
| TEN-005 | audit transversal |

## 41. Conclusion

La Partie 2 crée le langage commun de BIE.

Toutes les autres parties doivent utiliser les mêmes objets, identifiants et relations. C'est cette discipline qui rend possibles la génération automatique des instruments, le Knowledge Graph, le suivi longitudinal et l'orchestration par IA.
