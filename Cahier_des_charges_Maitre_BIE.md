---
title: "Budget Intelligence Engine — Cahier des charges maître"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Budget Intelligence Engine — Cahier des charges maître

## Plateforme nationale d’intelligence des finances publiques et de la performance

## 0. Vision exécutive

**Budget Intelligence Engine (BIE)** est une plateforme intégrée de planification, costing, budgétisation, passation, programmation financière, suivi, performance, évaluation, données et intelligence artificielle.

> **« De la Vision nationale au franc programmé ; du franc dépensé au résultat obtenu. »**

BIE complète les systèmes transactionnels officiels ; il ne les duplique pas.

## 1. Chaîne cible

```text
VISION BÉNIN 2060
        ↓
PND
        ↓
POLITIQUES / STRATÉGIES
        ↓
DPBEP / DPPD
        ↓
PAP
        ↓
PTA / PTAB
        ↓
TÂCHES
        ↓
COSTING
        ↓
BUDGET
        ↓
PPM / PCC / AE-CP
        ↓
CONTRATS / EXÉCUTION
        ↓
PERFORMANCE
        ↓
ÉVALUATION
        ↓
APPRENTISSAGE
```

## 2. Principes maîtres

1. Data Once.
2. Rules First, AI Second.
3. Human-in-the-Loop.
4. API First.
5. Auditability by Design.
6. Security by Design.
7. Modular Monolith First.
8. Open Standards.
9. Provider Independence.
10. Complexité dans le moteur, simplicité dans l'interface.

## 3. Positionnement

### SIGFP

Source transactionnelle officielle lorsque compétente.

### SYCOREF

Source/référence de costing via intégration lorsque les données sont accessibles.

### e-Procurement

Système officiel de passation ; BIE prépare le PPM et récupère les statuts.

### PAG

Interopérabilité pour la programmation et le suivi des projets gouvernementaux.

## 4. Modèle canonique

```text
Vision
→ Objective
→ Policy
→ Mission
→ Program
→ Action
→ BudgetActivity
→ OperationalActivity
→ Task
→ Resource
→ Cost
→ Budget
→ Procurement
→ Contract
→ Payment
→ Output
→ Outcome
```

## 5. Domaines fonctionnels

```text
Strategy
Programmatic
Planning
Costing
Budget
Treasury/PCC
Procurement
Public Investment
Monitoring
Performance
Evaluation
Documents
Data
AI
Administration
```

## 6. Planning

BIE prépare DPPD, PAP, PTA/PTAB à partir d'un référentiel commun.

L'IA propose des tâches à partir de l'historique et du contexte stratégique.

## 7. Costing

```text
Task
→ Resource
→ Quantity
→ Reference Price
→ Cost
```

Tout prix important doit avoir une provenance.

## 8. Budget

BIE distingue :

```text
Need
Ceiling
Allocation
Available
```

et intègre :

- classifications ;
- financements ;
- AE/CP ;
- scénarios.

## 9. PCC

Le PCC est dérivé :

```text
Task calendar
+
Procurement calendar
+
Contract payments
```

## 10. Procurement

```text
PTAB
→ External needs
→ Aggregation
→ PPM
→ Procedure
→ Contract
```

Les seuils sont versionnés dans un Rules Engine.

## 11. Public Investment

```text
Idea
→ Maturity
→ Appraisal
→ Project Bank
→ Prioritization
→ PIP
→ Procurement
→ Execution
→ Ex-post
```

## 12. Monitoring

Suivi simultané :

```text
Physical
Financial
Procurement
Performance
```

## 13. Performance

```text
Objective
→ Indicator
→ Target
→ Observation
→ Gap
```

## 14. Evaluation

Support :

- ex ante ;
- processus ;
- mi-parcours ;
- finale ;
- ex-post ;
- impact.

Les recommandations sont suivies jusqu'à clôture.

## 15. Data Platform

```text
PostgreSQL
ClickHouse
S3
Iceberg/Parquet
Qdrant
Neo4j
```

## 16. Knowledge

BIE transforme les documents en données et connaissances citables, tout en conservant leurs pages, versions, statuts et provenance.

## 17. Hybrid RAG

```text
Exact
+
Lexical
+
Dense
+
Sparse
+
Filters
+
Reranking
```

Le router choisit SQL, documents, graph ou règles selon la question.

## 18. BIE AI OS

```text
Budget Copilot
AI Gateway
Model Router
Agent Orchestrator
Tool Registry
Memory
Evals
Guardrails
```

## 19. Agents

```text
Planning
Costing
Budget
Procurement
Investment
Performance
Risk
Data
Knowledge
Document
Simulation
Verification
```

## 20. AI Actions

```text
AI proposal
 ↓
Schema
 ↓
Permission
 ↓
Rules
 ↓
Human approval when required
 ↓
Domain command
```

## 21. Security

Architecture Zero Trust avec :

```text
Keycloak
OPA
MFA
RBAC/ABAC
Vault/KMS
Cilium
Falco
Immutable Audit
DevSecOps
PRA/PCA
```

## 22. Infrastructure

```text
Next.js / React / TypeScript
Kotlin / Spring Boot
Python / FastAPI
Temporal
Redpanda
PostgreSQL
ClickHouse
Qdrant
Neo4j
S3
Kubernetes
```

## 23. UX

Un workspace unique, centré sur les rôles, avec :

```text
Table
Tree
Gantt
Calendar
Map
Dashboard
Copilot
```

Excel reste un format d'échange, pas la source maîtresse.

## 24. Reporting

Génération :

```text
PDF
DOCX
XLSX
CSV
PPTX
```

Les chiffres proviennent des données structurées.

## 25. Administration

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

## 26. NFR

Cibles indicatives :

```text
Availability ≥ 99.9%
Simple read P95 ≤ 2s
Standard write P95 ≤ 3s
Dashboard P95 ≤ 3s
50,000+ users
```

## 27. Testing

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

## 28. Migration

```text
Inventory
→ Profile
→ Map
→ Clean
→ Transform
→ Load
→ Reconcile
→ Accept
```

Aucune perte silencieuse.

## 29. Change Management

```text
Communication
Training
Super Users
Train-the-Trainer
Support
Feedback
Adoption KPIs
```

## 30. Governance

Instances :

```text
Steering Committee
Product Council
Architecture Review Board
Data Governance
AI Governance
Security Governance
```

## 31. Roadmap

```text
Phase 0 Blueprint
Phase 1 MVP
Phase 2 Ministry Pilot
Phase 3 Multi-Ministry
Phase 4 National Core
Phase 5 Extended Public Sector
Phase 6 Advanced Intelligence
```

## 32. MVP

```text
IAM
Organizations
Programmatic Registry
PTAB
Task suggestions
Costing
Envelope controls
PPM draft
PCC basic
Monitoring
Audit
Document RAG
Core Copilot
```

## 33. Stage Gates

Pas de déploiement national en big bang.

Chaque phase doit démontrer :

```text
functionality
security
adoption
data quality
performance
value
```

## 34. KPI

### Adoption

```text
Active users / target users
```

### Data Once

```text
% PPM generated from PTAB
```

### Costing

```text
% activities with documented costing
```

### Performance

```text
% expenditure linked to measurable outputs/results
```

### AI

```text
acceptance rate
edit rate
cost per accepted output
```

## 35. North Star

> **Part des dépenses publiques entièrement traçables de la priorité stratégique au résultat obtenu.**

## 36. Catalogue maître des exigences

### STR
- STR-001 : référentiel stratégique.
- STR-002 : alignement.
- STR-003 : justification.
- STR-004 : gap detection.
- STR-005 : versioning.

### ORG
- ORG-001 : multi-tenant.
- ORG-002 : structures.
- ORG-003 : rôles temporels.
- ORG-004 : rôle/personne.
- ORG-005 : exercices/scénarios.

### PLN
- PLN-001 : PTA/PTAB.
- PLN-002 : activités.
- PLN-003 : tâches.
- PLN-004 : dépendances.
- PLN-005 : calendrier.
- PLN-006 : suggestions IA.
- PLN-007 : versions.
- PLN-008 : validations.

### CST
- CST-001 : déterminants.
- CST-002 : ressources.
- CST-003 : prix.
- CST-004 : provenance.
- CST-005 : anomalies.
- CST-006 : historique.
- CST-007 : SYCOREF.
- CST-008 : IA.

### BUD
- BUD-001 : enveloppes.
- BUD-002 : allocations.
- BUD-003 : classifications.
- BUD-004 : financement.
- BUD-005 : disponibilité.
- BUD-006 : AE/CP.
- BUD-007 : révisions.
- BUD-008 : simulations.

### PCC
- PCC-001 : calendrier mensuel.
- PCC-002 : trimestriel.
- PCC-003 : activités.
- PCC-004 : marchés.
- PCC-005 : contrats.
- PCC-006 : cash forecast.

### PRC
- PRC-001 : besoins PTAB.
- PRC-002 : agrégation.
- PRC-003 : anti-splitting.
- PRC-004 : mutualisation.
- PRC-005 : seuils.
- PRC-006 : calendrier.
- PRC-007 : documents.
- PRC-008 : e-procurement.

### CTR
- CTR-001 : contrat.
- CTR-002 : jalons.
- CTR-003 : paiements.
- CTR-004 : avenants.
- CTR-005 : garanties.
- CTR-006 : réception.
- CTR-007 : performance.

### INV
- INV-001 : project bank.
- INV-002 : maturité.
- INV-003 : appraisal.
- INV-004 : priorisation.
- INV-005 : PIP.
- INV-006 : packages.
- INV-007 : AE/CP.
- INV-008 : ex-post.

### MON
- MON-001 : physique.
- MON-002 : financier.
- MON-003 : passation.
- MON-004 : preuves.
- MON-005 : variances.
- MON-006 : causes.
- MON-007 : alertes.
- MON-008 : actions correctives.

### PERF
- PERF-001 : objectifs.
- PERF-002 : indicateurs.
- PERF-003 : targets.
- PERF-004 : observations.
- PERF-005 : efficacité.
- PERF-006 : efficience.
- PERF-007 : coûts unitaires.
- PERF-008 : forecast.

### EVA
- EVA-001 : calendrier.
- EVA-002 : méthodes.
- EVA-003 : preuves.
- EVA-004 : findings.
- EVA-005 : recommandations.
- EVA-006 : management response.
- EVA-007 : suivi.

### DATA
- DATA-001 : PostgreSQL.
- DATA-002 : Lakehouse.
- DATA-003 : ClickHouse.
- DATA-004 : Qdrant.
- DATA-005 : Neo4j.
- DATA-006 : metadata.
- DATA-007 : lineage.
- DATA-008 : quality.
- DATA-009 : provenance.
- DATA-010 : versioning.

### AI
- AI-001 : Copilot.
- AI-002 : Gateway.
- AI-003 : Routing.
- AI-004 : Agents.
- AI-005 : Tools.
- AI-006 : RAG.
- AI-007 : GraphRAG.
- AI-008 : Evals.
- AI-009 : Human approval.
- AI-010 : Audit.

### SEC
- SEC-001 : Zero Trust.
- SEC-002 : MFA.
- SEC-003 : RBAC/ABAC.
- SEC-004 : OPA.
- SEC-005 : secrets.
- SEC-006 : encryption.
- SEC-007 : immutable audit.
- SEC-008 : DevSecOps.
- SEC-009 : PRA.
- SEC-010 : PSSIE.

### UX
- UX-001 : role-based.
- UX-002 : tables.
- UX-003 : Excel.
- UX-004 : dashboards.
- UX-005 : maps.
- UX-006 : PWA.
- UX-007 : offline.
- UX-008 : accessibility.

### INT
- INT-001 : SIGFP.
- INT-002 : SYCOREF.
- INT-003 : e-procurement.
- INT-004 : PAG.
- INT-005 : APIs.
- INT-006 : events.
- INT-007 : canonical model.

### NFR
- NFR-001 : availability.
- NFR-002 : performance.
- NFR-003 : scalability.
- NFR-004 : observability.
- NFR-005 : maintainability.
- NFR-006 : portability.
- NFR-007 : accessibility.
- NFR-008 : resilience.

## 37. Backlog initial

```text
EPIC 01 Identity & Organizations
EPIC 02 Programmatic Registry
EPIC 03 Strategic Alignment
EPIC 04 PTA/PTAB
EPIC 05 Task Intelligence
EPIC 06 Costing
EPIC 07 Budget & Envelopes
EPIC 08 PPM
EPIC 09 PCC
EPIC 10 Monitoring
EPIC 11 Performance
EPIC 12 Documents
EPIC 13 Knowledge & RAG
EPIC 14 Budget Copilot
EPIC 15 Audit & Security
EPIC 16 Integrations
EPIC 17 Reporting
EPIC 18 Administration
```

## 38. Conclusion

BIE doit transformer une administration orientée documents et ressaisies en une administration fondée sur des données structurées, des règles contrôlées, une intelligence assistive et une traçabilité complète.

La responsabilité reste humaine ; BIE améliore la qualité des informations et la capacité d'anticipation.

# BUDGET INTELLIGENCE ENGINE

## NATIONAL PUBLIC FINANCE & PERFORMANCE INTELLIGENCE PLATFORM

### « De la Vision nationale au franc programmé ; du franc dépensé au résultat obtenu. »
