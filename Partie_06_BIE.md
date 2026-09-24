---
title: "Partie 6 — Monitoring, Evaluation & Performance Engine"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 6 — Monitoring, Evaluation & Performance Engine

## Exécution physique, financière, performance, risques, évaluations et apprentissage

## 1. Objet

Le MEP Engine doit dépasser le simple taux d'exécution budgétaire et relier :

```text
PLAN
 ↓
EXECUTION
 ↓
OUTPUT
 ↓
OUTCOME
 ↓
IMPACT
 ↓
EVALUATION
 ↓
LEARNING
```

## 2. Trois niveaux

```text
MONITORING
PERFORMANCE MANAGEMENT
EVALUATION
```

## 3. Results Chain

```text
INPUTS
 ↓
ACTIVITIES
 ↓
OUTPUTS
 ↓
OUTCOMES
 ↓
IMPACTS
```

## 4. Theory of Change

BIE doit représenter :

- problème ;
- intervention ;
- mécanismes ;
- hypothèses ;
- facteurs externes ;
- résultats.

## 5. Milestone

```text
label
planned_date
actual_date
weight
status
evidence
```

## 6. Physical Progress

Méthodes :

- quantités ;
- jalons pondérés ;
- livrables ;
- bénéficiaires.

## 7. Pas de taux opaque

Un taux physique doit être explicable par une formule ou des jalons lorsque cela est possible.

## 8. Evidence

Pièces possibles :

- PV ;
- rapport ;
- photo ;
- GPS ;
- certificat ;
- fichier.

## 9. Planned vs Actual

Comparer :

```text
Dates
Quantities
Costs
Indicators
Procurement
```

## 10. Variance Engine

Types :

```text
SCHEDULE
COST
PHYSICAL
PERFORMANCE
PROCUREMENT
```

## 11. Root Cause

Causes structurées :

```text
budget
procurement
technical
administrative
external
data
```

L'IA peut proposer une cause probable ; l'utilisateur confirme.

## 12. Physical-Financial Matrix

Comparer systématiquement :

```text
PHYSICAL
vs
FINANCIAL
```

## 13. Procurement Dimension

Ajouter le statut de passation pour interpréter correctement les retards.

## 14. Indicator Observation

```text
indicator
period
value
source
method
territory
disaggregation
validation
```

## 15. Frequency

```text
REAL_TIME
DAILY
WEEKLY
MONTHLY
QUARTERLY
SEMI_ANNUAL
ANNUAL
AD_HOC
```

## 16. Automatic Collection

Sources :

- SIGFP ;
- e-procurement ;
- APIs ;
- systèmes sectoriels ;
- statistiques.

## 17. Formula Engine

Les taux dérivables ne doivent pas être saisis manuellement.

## 18. Disaggregation

Selon disponibilité et cadre légal :

```text
sex
age
territory
beneficiary type
disability
```

## 19. Baseline et targets

Chaque série peut porter :

```text
baseline
target trajectory
actual observations
```

## 20. Performance Gap

```text
Actual - Target
```

avec interprétation dépendant du sens de l'indicateur.

## 21. Forecast

BIE peut produire une prévision clairement étiquetée comme telle.

## 22. Indicator Quality

Contrôler :

- définition ;
- formule ;
- source ;
- fréquence ;
- baseline ;
- cible.

## 23. SMART Assistance

L'IA peut aider à améliorer la qualité rédactionnelle d'un indicateur.

## 24. Output vs Outcome Balance

BIE signale les cadres de performance composés uniquement d'indicateurs d'activité.

## 25. Early Warning

Sources :

```text
Planning
Budget
Procurement
Contract
Physical
Indicator
Risk
```

## 26. EarlyWarningSignal

```text
signal_type
severity
probability
impact
evidence
status
```

## 27. Risk Register

```text
risk
owner
probability
impact
mitigation
deadline
residual_risk
```

## 28. CorrectiveAction

```text
issue
action
responsible
deadline
status
```

## 29. Recommendation Tracking

Les recommandations issues d'audits, évaluations et conférences sont structurées et suivies.

## 30. Monitoring Cycles

```text
Monthly
Quarterly
Mid-year
Annual
Ad hoc
```

## 31. RAP / RAPEX / RaNaP

BIE doit préparer automatiquement les datasets et canevas nécessaires à la reddition de comptes.

## 32. PAP ↔ RAP

```text
PAP = promise
Execution = action
RAP = result
```

## 33. RAP Skeleton

```text
Program strategy
Objective
Indicator
Target
Actual
Budget
Execution
Variance
Explanation
```

## 34. Efficiency

Analyser :

```text
Resources
vs
Outputs
```

Exemples :

- coût par bénéficiaire ;
- coût par km ;
- coût par inspection.

## 35. Effectiveness

Analyser l'atteinte des objectifs et résultats.

## 36. Evaluation Criteria

Support des critères :

```text
Relevance
Coherence
Effectiveness
Efficiency
Impact
Sustainability
```

## 37. Evaluation Object

```text
intervention
type
scope
period
methodology
questions
team
status
```

## 38. Evaluation Types

```text
EX_ANTE
PROCESS
MID_TERM
FINAL
EX_POST
IMPACT
THEMATIC
RAPID
```

## 39. Evaluation Matrix

| Question | Critère | Indicateur | Source | Méthode |
|---|---|---|---|---|

## 40. Findings and Recommendations

```text
Evidence
 ↓
Finding
 ↓
Conclusion
 ↓
Recommendation
 ↓
Management Response
```

## 41. Learning Loop

```text
Evaluation
 ↓
Recommendation
 ↓
Decision
 ↓
New Planning
```

## 42. Data Collection

Canaux :

```text
WEB
MOBILE
API
IMPORT
OFFLINE
```

## 43. Field Collection

Fonctions :

- GPS ;
- photo ;
- signature ;
- formulaire ;
- offline.

## 44. Data Quality

Dimensions :

```text
Completeness
Accuracy
Consistency
Timeliness
Uniqueness
Validity
```

## 45. Validation Workflow

```text
ENTERED
 ↓
REVIEWED
 ↓
VALIDATED
 ↓
OFFICIAL
```

## 46. Territorial Intelligence

Analyser les résultats par :

```text
Country
Department
Commune
Arrondissement
```

## 47. Cross-Cutting Dimensions

Support :

- genre ;
- climat ;
- social ;
- enfant ;
- handicap ;
- ODD.

## 48. Dashboards

Niveaux :

```text
Executive
DPAF
RPRO
M&E
National
```

## 49. Copilot

Questions :

> Quels programmes sont à risque ?

> Pourquoi la cible n'est-elle pas atteinte ?

> Quelles recommandations restent ouvertes ?

Les réponses doivent utiliser les outils analytiques, pas seulement le langage naturel.

## 50. Statistical Workspace

BIE peut interagir avec Python/R dans un environnement contrôlé pour les analyses avancées.

## 51. No Causal Hallucination

Corrélation ≠ causalité. Les évaluations d'impact doivent expliciter leur méthode.

## 52. Requirements

### MON

| ID | Exigence |
|---|---|
| MON-001 | suivi physique |
| MON-002 | suivi financier |
| MON-003 | suivi passation |
| MON-004 | preuves |
| MON-005 | écarts |
| MON-006 | causes |
| MON-007 | alertes |
| MON-008 | actions correctives |

### PERF

| ID | Exigence |
|---|---|
| PERF-001 | indicateurs |
| PERF-002 | targets |
| PERF-003 | scorecards |
| PERF-004 | efficacité |
| PERF-005 | efficience |
| PERF-006 | coûts unitaires |
| PERF-007 | tendances |
| PERF-008 | forecasting |

### EVA

| ID | Exigence |
|---|---|
| EVA-001 | calendrier |
| EVA-002 | méthodologies |
| EVA-003 | preuves |
| EVA-004 | findings |
| EVA-005 | recommandations |
| EVA-006 | management response |
| EVA-007 | suivi |

## 53. Conclusion

BIE doit permettre de répondre :

> **Un milliard dépensé a produit quoi, où, pour qui, à quel coût et avec quel résultat ?**

C'est cette boucle qui transforme un système de programmation budgétaire en plateforme d'intelligence de la performance publique.
