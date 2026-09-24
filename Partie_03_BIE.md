---
title: "Partie 3 — Strategic & Operational Planning Engine"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 3 — Strategic & Operational Planning Engine

## Vision 2060, PND, DPBEP/DPPD, PAP, PTA/PTAB et préparation budgétaire

## 1. Objet

Cette partie décrit le moteur de planification stratégique et opérationnelle de BIE.

La plateforme doit relier une activité annuelle aux priorités stratégiques dont elle découle et permettre de préparer les instruments sans ressaisie.

## 2. Chaîne stratégique de référence

```text
Vision Bénin 2060
        ↓
PND 2026-2035
        ↓
Politiques et stratégies sectorielles
        ↓
Programme gouvernemental
        ↓
DPBEP
        ↓
DPPD
        ↓
PAP
        ↓
PTA / PTAB
```

## 3. Strategic Registry

BIE doit gérer un référentiel versionné de :

- vision ;
- orientations ;
- objectifs stratégiques ;
- axes PND ;
- politiques ;
- stratégies ;
- programmes gouvernementaux.

## 4. Strategic Alignment Engine

Toute activité ou projet peut être rattaché à plusieurs niveaux.

```text
Activity
 ─CONTRIBUTES_TO→ Program Objective
 ─CONTRIBUTES_TO→ Sector Strategy
 ─CONTRIBUTES_TO→ PND Objective
```

## 5. Strategic Alignment Score

Score analytique basé sur :

- liens déclarés ;
- mots-clés ;
- résultats attendus ;
- indicateurs ;
- historique.

Le score n'est pas une validation politique.

## 6. Gap Detection

BIE peut signaler :

- objectif stratégique sans programmation ;
- activité non alignée ;
- programme sans indicateur ;
- priorité sans financement identifiable.

## 7. Programmatic Registry

Modèle :

```text
Mission
 ↓
Program
 ↓
Objective
 ↓
Indicator
 ↓
Action
 ↓
BudgetActivity
```

## 8. DPPD Workspace

Le module DPPD devra permettre :

- gestion de la stratégie du programme ;
- objectifs ;
- actions ;
- indicateurs ;
- projections pluriannuelles ;
- priorités ;
- risques ;
- narratifs.

## 9. PAP Workspace

Le PAP réutilise les mêmes objets.

Principe :

```text
DPPD data
+
Annual targets
+
Annual allocation
=
PAP view
```

## 10. PTA/PTAB

Le PTAB représente l'opérationnalisation annuelle.

```text
BudgetActivity
 ↓
OperationalActivity
 ↓
Tasks
```

## 11. Création guidée d'activité

L'utilisateur renseigne :

```text
Activity label
Responsible structure
Expected result
Calendar
Execution mode
Territory
```

BIE propose ensuite le reste lorsque possible.

## 12. Similar Activity Retrieval

Le système recherche :

- même libellé ;
- même domaine ;
- même structure ;
- même résultat ;
- activités sémantiquement proches.

## 13. Task Suggestion Engine

Pipeline :

```text
Activity
 ↓
Historical retrieval
 ↓
Relevant examples
 ↓
AI proposal
 ↓
Human review
```

## 14. Task Proposal

Chaque tâche proposée doit indiquer :

```text
label
sequence
reason
historical examples
confidence
```

## 15. Dependencies

Les dépendances pourront être explicites.

```text
Study
 ↓
Validation
 ↓
Procurement
 ↓
Execution
```

## 16. Activity Templates

Des modèles peuvent exister pour :

- formations ;
- ateliers ;
- missions ;
- études ;
- travaux ;
- acquisitions ;
- campagnes.

## 17. Template ≠ obligation

L'utilisateur peut adapter le modèle.

## 18. Calendar Engine

Chaque activité possède :

```text
start
end
quarters
milestones
dependencies
```

## 19. Budget Calendar

BIE intègre le calendrier annuel de préparation.

Exemple :

```text
DPPD
PAP
PTAB
PPM
PCC
Conferences
Arbitrations
```

## 20. Deadline Engine

Le système calcule :

- délai restant ;
- tâches en retard ;
- dépendances à risque.

## 21. Performance Conference Workspace

BIE doit préparer les conférences avec :

```text
Program context
Historical performance
Targets
Budget
Activities
Risks
Open decisions
```

## 22. Conference Decisions

Les décisions prises sont enregistrées comme données structurées.

```text
Decision
Responsible
Deadline
Status
```

## 23. Baseline Planning

Pour N+1, BIE construit une baseline à partir de :

- activités récurrentes ;
- engagements pluriannuels ;
- projets en cours ;
- recommandations ;
- historique ;
- priorités nouvelles.

## 24. Continuing Commitments

Les engagements en cours doivent être intégrés avant de programmer librement de nouveaux besoins.

## 25. Planning Scenarios

Exemples :

```text
BASELINE
CEILING
REDUCED_10_PERCENT
PRIORITY_ONLY
```

## 26. Scenario Isolation

Une simulation ne modifie jamais le PTAB officiel.

## 27. Planning Optimization

BIE peut aider à optimiser sous contrainte :

```text
Budget ceiling
Mandatory commitments
Strategic priority
Expected result
```

Le calcul doit reposer sur un solver ou un algorithme déterministe lorsqu'il s'agit d'optimisation.

## 28. Execution Mode

Valeurs principales :

```text
DIRECT
INDIRECT
MIXED
```

Ce champ pilote notamment la génération du PPM.

## 29. Direct

Exécution principalement interne.

## 30. Indirect

Besoin externe générant un besoin de commande publique.

## 31. Mixed

Séparation entre composantes internes et externes.

## 32. PPM Derivation

```text
Task
 ↓
Resource
 ↓
External requirement
 ↓
ProcurementNeed
```

## 33. PCC Derivation

Le calendrier des tâches et de la commande publique prépare le calendrier de consommation.

## 34. Revision

BIE doit gérer :

```text
Initial plan
Revision R1
Revision R2
...
```

avec diff détaillé.

## 35. Diff

Afficher :

```text
Added
Removed
Modified amount
Modified period
Modified task
```

## 36. Validation

Workflow configurable :

```text
Draft
 ↓
Internal review
 ↓
Submission
 ↓
Control
 ↓
Approval
```

## 37. Comments

Chaque retour de validation peut être :

- général ;
- rattaché à une activité ;
- rattaché à une cellule/champ.

## 38. Bulk Editing

Les planificateurs doivent pouvoir modifier plusieurs lignes simultanément.

## 39. Excel Interoperability

Support :

```text
Import
Copy/Paste
Export
```

mais BIE reste la source maîtresse.

## 40. Planning Dashboard

Indicateurs :

```text
Activities complete
Missing tasks
Costing missing
Over ceiling
Strategic gap
Late validations
```

## 41. Requirements

| ID | Exigence |
|---|---|
| PLN-001 | gérer DPPD/PAP/PTA/PTAB |
| PLN-002 | générer baseline |
| PLN-003 | proposer tâches |
| PLN-004 | gérer dépendances |
| PLN-005 | gérer calendrier |
| PLN-006 | intégrer exécution directe/indirecte/mixte |
| PLN-007 | gérer scénarios |
| PLN-008 | gérer révisions |
| PLN-009 | gérer commentaires |
| PLN-010 | produire PPM/PCC downstream |

## 42. Conclusion

Le Planning Engine doit supprimer le traitement séparé des instruments. Le PTAB devient la représentation opérationnelle d'un modèle programmatique unique, relié à la stratégie, au coût, à la passation, à la trésorerie et à la performance.
