---
title: "Partie 10 — UX/UI, industrialisation, déploiement national, gouvernance, recette et roadmap"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 10 — UX/UI, industrialisation, déploiement national, gouvernance, recette et roadmap

## Transformer l'architecture BIE en produit réellement déployable

## 1. Objet

Cette partie couvre :

```text
UX/UI
Design System
Dashboards
Mobile/PWA
Reporting
Administration
NFR
QA
Migration
Training
Support
Governance
Roadmap
KPIs
```

## 2. Principe produit

> **Puissant dans le moteur, simple dans l'interface.**

## 3. One Workspace

L'utilisateur ne doit pas percevoir cinq applications séparées.

```text
ONE BIE WORKSPACE
```

## 4. Navigation

```text
Accueil
Stratégie
Planification
Budget
Passation
Investissements
Exécution
Performance
Données
Rapports
Copilot
Administration
```

## 5. Role-Based UX

Personas :

```text
Décideur
DPAF
DPPD
RPRO
Planificateur
S&E
PRMP
Finance
Contrôleur
Auditeur
Admin
```

## 6. My Workspace

Afficher :

```text
Actions required
Validations
Deadlines
Alerts
Recent work
```

## 7. Global Search

Recherche unique sur :

- programmes ;
- activités ;
- marchés ;
- projets ;
- documents ;
- indicateurs.

## 8. Command Palette

`Ctrl/Cmd + K` pour :

- créer ;
- ouvrir ;
- rechercher ;
- demander au Copilot.

## 9. Object 360

Exemple Activity 360 :

```text
Overview
Tasks
Costing
Budget
Procurement
Execution
Performance
Documents
History
```

## 10. Views

```text
TABLE
TREE
KANBAN
CALENDAR
GANTT
MAP
DASHBOARD
```

## 11. Advanced Table

Pour PTAB/PPM :

- virtualisation ;
- bulk edit ;
- filters ;
- grouping ;
- freeze ;
- copy/paste.

## 12. Excel Interoperability

```text
Import
Copy/Paste
Export
```

Excel reste un format d'échange, pas la source maîtresse.

## 13. PWA

Usages prioritaires :

- terrain ;
- consultation ;
- notifications ;
- petites validations.

## 14. Offline

Collecte terrain :

```text
download
collect
sync
```

## 15. Accessibility

Objectif :

```text
WCAG 2.2 AA
```

## 16. Design System

Créer :

```text
BIE Design System
```

avec :

- tokens ;
- composants ;
- documentation ;
- accessibilité.

## 17. Dashboards

Cockpits :

```text
Executive
Planning
Budget
Procurement
Investment
Performance
Territorial
National
```

## 18. Question-Oriented UX

Un dashboard répond à une question, pas seulement à une collection de KPI.

## 19. Drill Down

```text
National
 ↓
Ministry
 ↓
Program
 ↓
Activity
```

## 20. Dashboard Builder

Les utilisateurs habilités peuvent composer des vues à partir de métriques gouvernées.

## 21. Reporting

Formats :

```text
PDF
DOCX
XLSX
CSV
PPTX
```

## 22. Templates

Les modèles sont versionnés.

Les chiffres proviennent des données validées.

## 23. Export Center

Les exports volumineux sont des jobs asynchrones sécurisés.

## 24. Notifications

Canaux :

```text
IN_APP
EMAIL
PUSH
```

Catégories :

```text
ACTION_REQUIRED
DEADLINE
WARNING
CRITICAL
INFO
```

## 25. Collaboration

- commentaires ;
- mentions ;
- décisions ;
- tâches.

## 26. Administration

Modules :

```text
Organizations
Users
Roles
Fiscal Years
References
Rules
Workflows
Templates
Integrations
AI
Security
Audit
```

## 27. New Year Wizard

```text
Create fiscal year
Copy references
Load calendar
Load ceilings
Prepare baseline
```

## 28. Configuration Hierarchy

```text
National
 ↓
Institution
 ↓
Program
```

## 29. Help

- guides ;
- glossaire ;
- vidéos ;
- aide contextuelle.

## 30. Error UX

Les erreurs doivent être compréhensibles et localisées.

## 31. AI UX

Afficher clairement :

```text
AI Suggested
Why?
Sources
Confidence
Preview changes
```

## 32. UX Research

Processus :

```text
interview
prototype
usability test
implementation
```

## 33. Product Analytics

Mesurer :

- adoption ;
- completion ;
- erreurs ;
- usage ;

sans transformer BIE en outil de surveillance individuelle.

## 34. Non-Functional Requirements

Référence qualité : performance, disponibilité, maintenabilité, sécurité, accessibilité, portabilité, résilience.

## 35. Availability

Cible nationale indicative :

```text
≥ 99.9%
```

## 36. Performance

Cibles indicatives :

```text
Simple read P95 ≤ 2s
Standard write P95 ≤ 3s
Dashboard P95 ≤ 3s
```

## 37. Scalability

Dimensionnable vers :

```text
50,000+ registered users
```

## 38. Testing

```text
Unit
Rules
Integration
Contract
E2E
Performance
Security
Accessibility
AI Evals
UAT
```

## 39. UAT

Parcours critique :

```text
Activity
→ Tasks
→ Costing
→ Budget
→ PPM
→ PCC
→ Monitoring
```

## 40. Production Gate

Pas de go-live avec un BLOCKER non accepté formellement.

## 41. Migration

```text
DISCOVER
PROFILE
MAP
CLEAN
TRANSFORM
LOAD
RECONCILE
ACCEPT
```

## 42. Zero Silent Loss

Toute donnée rejetée est expliquée.

## 43. Historical Backfill

Priorité aux exercices les plus utiles à :

- RAG ;
- costing ;
- comparaisons ;
- prédictions.

## 44. Change Management

```text
Communication
Training
Champions
Support
Feedback
Adoption metrics
```

## 45. Train-the-Trainer

Créer des BIE Super Users dans chaque institution.

## 46. Support

```text
L0 Self-service
L1 Institutional
L2 BIE
L3 Engineering
```

## 47. Governance

Instances :

```text
National Steering Committee
Product Council
Architecture Review Board
AI Governance
Data Governance
Security Governance
```

## 48. Product Team

Composition indicative :

```text
Product Lead
Architect
Backend
Frontend
AI
Data
DevSecOps/SRE
QA
UX
Business experts
```

## 49. Roadmap

```text
PHASE 0 Blueprint
 ↓
PHASE 1 MVP
 ↓
PHASE 2 Ministry Pilot
 ↓
PHASE 3 Multi-Ministry
 ↓
PHASE 4 National Core
 ↓
PHASE 5 Extended Public Sector
 ↓
PHASE 6 Advanced Intelligence
```

## 50. Blueprint

Durée indicative :

```text
8–12 weeks
```

## 51. MVP

Durée indicative :

```text
4–6 months
```

Périmètre :

```text
IAM
Organizations
Programmatic registry
PTAB
Task suggestions
Costing
Envelope controls
PPM draft
Monitoring
Document RAG
Core Copilot
Audit
```

## 52. Pilot

Un exercice budgétaire réel doit être traité.

## 53. Stage Gates

Chaque passage dépend de :

```text
functionality
security
adoption
data quality
performance
value
```

## 54. No Big Bang

Le national se fait par vagues.

## 55. One Product, No Ministry Forks

```text
ONE CORE
+
CONFIGURATION
```

## 56. Benefits Realization

Mesures :

- temps gagné ;
- ressaisies évitées ;
- qualité ;
- adoption ;
- exécution ;
- performance.

## 57. North Star

À maturité :

> **Part des dépenses publiques entièrement traçables de la priorité stratégique au résultat obtenu.**

## 58. Budget indicatif

Ordres de grandeur préliminaires :

```text
Blueprint         50–100 M FCFA
MVP               250–500 M
Pilot             +300–700 M
Multi-ministry    +600 M–1.5 Md
Nationalization   +1.5–4 Md+
```

À recalculer après Blueprint.

## 59. Acceptance Domains

```text
Functional
Data
Integration
AI
UX
Security
Performance
Resilience
Documentation
Adoption
```

## 60. Final Vision

```text
Strategy
 ↓
Planning
 ↓
Costing
 ↓
Budget
 ↓
Procurement / Cash
 ↓
Execution
 ↓
Performance
 ↓
Evaluation
 ↓
Learning
 ↺
```

## 61. Conclusion

Le succès de BIE sera atteint lorsque la préparation d'un nouvel exercice ne commencera plus par « envoyez-moi le fichier Excel de l'année précédente », mais par l'exploitation d'une mémoire publique structurée, traçable et intelligente.
