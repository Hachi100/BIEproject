---
title: "Partie 4 — Costing, Budget, AE/CP, programmation financière et PCC"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 4 — Costing, Budget, AE/CP, programmation financière et PCC

## Du besoin physique au financement et au calendrier de consommation

## 1. Objet

La Partie 4 définit le moteur de **costing**, le Budget Engine, la gestion des enveloppes, les classifications, les financements, l'AE/CP et le PCC.

Le principe fondamental est :

```text
Task
 ↓
Physical determinant
 ↓
Resource
 ↓
Quantity
 ↓
Reference price
 ↓
Cost
 ↓
Budget classification
 ↓
Funding
 ↓
Payment schedule
```

## 2. Costing par déterminants physiques

BIE ne doit pas accepter par défaut un montant forfaitaire sans explication lorsque l'activité peut être décomposée.

Exemple :

```text
60 participants
× 3 days
× unit costs
```

## 3. CostingLine

```text
task
resource
quantity
unit
unit_price
price_source
adjustment
total
```

## 4. Resource Catalog

Catalogue commun :

- mission ;
- transport ;
- restauration ;
- hébergement ;
- fournitures ;
- matériels ;
- prestations ;
- travaux.

## 5. Price Reference Registry

Sources possibles :

```text
Official reference
SYCOREF
Historical contract
Approved internal price
Market observation
Technical estimate
```

## 6. Price Provenance

Chaque prix conserve :

```text
source
reference
year
territory
unit
validity
```

## 7. Price Search

L'utilisateur peut rechercher par :

- libellé ;
- référence ;
- catégorie ;
- territoire ;
- année.

## 8. Historical Cost Intelligence

BIE analyse :

```text
estimated price
contract price
paid price
```

## 9. Price Anomaly

Exemple :

```text
Reference = 525 000
Proposed = 700 000
```

Le système signale l'écart et demande une justification.

## 10. Confidence

Les prix peuvent être accompagnés d'un niveau de confiance selon :

- fraîcheur ;
- source ;
- similarité ;
- nombre d'observations.

## 11. Costing Template

Des ressources typiques peuvent être proposées pour une activité historique similaire.

## 12. AI Costing Agent

L'IA peut :

- proposer ressources ;
- expliquer ;
- rechercher précédents ;
- proposer quantités.

Elle ne doit pas inventer une valeur transactionnelle sans source.

## 13. Budget Need

Le coût total constitue un **besoin**, distinct de l'enveloppe.

```text
Need
≠
Ceiling
≠
Allocation
```

## 14. BudgetEnvelope

```text
initial_ceiling
current_ceiling
allocated
reserved
available
```

## 15. Envelope Hierarchy

Les enveloppes peuvent être suivies :

```text
Institution
Program
Action
Structure
Budget line
```

## 16. Real-Time Control

À chaque costing :

```text
new need
→ recompute availability
```

## 17. Over-Ceiling

Si :

```text
Need > Available
```

le système :

- alerte ;
- empêche certaines validations ;
- permet simulation/arbitrage.

## 18. Budget Classification

BIE doit intégrer :

```text
Administrative
Programmatic
Economic
Functional
Geographic
Funding
```

## 19. AI-Assisted Imputation

Pipeline :

```text
Task description
 ↓
AI candidate
 ↓
Rules validation
 ↓
Human confirmation
```

## 20. FundingSource

Une activité peut utiliser plusieurs sources.

```text
National budget
Loan
Grant
Counterpart
```

## 21. FundingPlan

```text
Need = 500 M
Budget = 300 M
Donor = 150 M
Gap = 50 M
```

## 22. Funding Gap

BIE doit expliciter le besoin non couvert.

## 23. Budget Reservation

Lorsqu'un projet de programmation est suffisamment avancé, le système peut réserver analytiquement une partie de l'enveloppe sans confondre cette réservation avec un engagement comptable officiel.

## 24. Reallocation Scenario

Le système peut simuler :

```text
Reduce activity A
Increase activity B
```

et montrer l'impact.

## 25. AE / CP

BIE doit distinguer :

```text
AE = authorization to commit
CP = payment credit
```

## 26. Multi-Year Project

Exemple :

```text
AE total: 10 Bn
CP 2028: 2 Bn
CP 2029: 5 Bn
CP 2030: 3 Bn
```

## 27. CP Profile

Le profil de CP doit être relié au calendrier réel :

```text
procurement
contract
milestone
payment
```

## 28. PCC

Le Plan de Consommation de Crédits doit devenir une vue dérivée du modèle de planification et d'exécution.

## 29. PCC Sources

```text
Task calendar
Procurement calendar
Contract milestones
Expected payment date
```

## 30. Monthly Profile

```text
Jan 10 M
Feb 15 M
Mar 35 M
```

## 31. Quarterly Profile

Agrégation automatique.

## 32. Cash Forecast

BIE distinguera :

```text
allocation
commitment
liquidation
payment
cash need
```

selon données disponibles et responsabilités des systèmes officiels.

## 33. Financial Programming

Objet :

`FinancialSchedule`

avec :

```text
period
planned_amount
funding_source
related_object
status
```

## 34. Rescheduling

Déplacer une activité ou un marché doit pouvoir recalculer :

- PCC ;
- cash forecast ;
- CP ;
- prévisions.

## 35. Scenario

Question :

> Quel est l'impact si ce marché est décalé de deux mois ?

BIE doit recalculer le profil de consommation.

## 36. Budget Revision

Une modification d'enveloppe doit être versionnée et propager ses impacts.

## 37. Cost Reconciliation

Comparer :

```text
planned cost
contract amount
actual paid
```

## 38. Cost Learning

Les coûts réels alimentent la mémoire des exercices futurs.

## 39. Dashboards

### Costing

```text
Activities without costing
Price anomalies
Average reference age
```

### Budget

```text
Need
Ceiling
Allocation
Available
Gap
```

### PCC

```text
Monthly forecast
Actual consumption
Variance
```

## 40. Requirements

### CST

| ID | Exigence |
|---|---|
| CST-001 | costing par déterminants |
| CST-002 | ressources |
| CST-003 | prix |
| CST-004 | provenance |
| CST-005 | anomalies |
| CST-006 | historique |
| CST-007 | intégration SYCOREF |
| CST-008 | propositions IA |

### BUD

| ID | Exigence |
|---|---|
| BUD-001 | enveloppes |
| BUD-002 | allocations |
| BUD-003 | classifications |
| BUD-004 | financements |
| BUD-005 | disponibilité |
| BUD-006 | AE/CP |
| BUD-007 | scénarios |
| BUD-008 | révisions |

### PCC

| ID | Exigence |
|---|---|
| PCC-001 | profil mensuel |
| PCC-002 | profil trimestriel |
| PCC-003 | liens activités |
| PCC-004 | liens marchés |
| PCC-005 | liens contrats |
| PCC-006 | forecast |
| PCC-007 | recalcul automatique |

## 41. Conclusion

La Partie 4 doit permettre de répondre à trois questions avant toute validation :

1. **Pourquoi ce coût ?**
2. **Sur quelle enveloppe ?**
3. **Quand ce financement sera-t-il consommé ?**

Ainsi, le budget ne repose plus sur un montant isolé mais sur une chaîne de déterminants, de références, de financement et de calendrier.
