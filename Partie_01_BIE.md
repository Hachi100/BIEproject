---
title: "Partie 1 — Vision stratégique, principes directeurs et architecture technologique de référence"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 1 — Vision stratégique, principes directeurs et architecture technologique de référence

## Fondations du Budget Intelligence Engine

## 1. Objet

Cette partie fixe la vision stratégique, les principes d'architecture et la doctrine technologique de **Budget Intelligence Engine (BIE)**.

BIE n'est pas un simple formulaire de saisie du PTAB, ni un chatbot, ni un substitut immédiat à SIGFP, SYCOREF ou au système national de commande publique. Il constitue une **plateforme nationale d'intelligence des finances publiques et de la performance**, positionnée en amont et en transversal des systèmes transactionnels officiels.

> **Signature conceptuelle : « De la Vision nationale au franc programmé ; du franc dépensé au résultat obtenu. »**

## 2. Vision cible

BIE doit relier dans une chaîne continue :

```text
VISION BÉNIN 2060
        ↓
PND / POLITIQUES SECTORIELLES
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
PPM / PCC
        ↓
EXÉCUTION
        ↓
PERFORMANCE
        ↓
ÉVALUATION
        ↓
APPRENTISSAGE
```

## 3. Positionnement de BIE

BIE combine quatre fonctions :

1. **Transaction intelligente** : création, validation, révision et suivi des objets métier.
2. **Data & Analytics** : consolidation, tableaux de bord, comparaison et détection d'anomalies.
3. **AI Assistance** : suggestions, recherche, génération, explication, prédiction et simulation.
4. **Orchestration** : workflows, validations, règles, événements et interopérabilité.

## 4. Principes directeurs

### 4.1 Domain First

La conception doit partir du métier :

```text
problème métier
→ processus
→ données
→ règles
→ automatisation
→ intelligence
→ technologie
```

### 4.2 Rules First, AI Second

Les règles légales, réglementaires et métier déterministes constituent l'autorité. L'IA ne les remplace pas.

### 4.3 Data Once

Une donnée saisie ou synchronisée une fois doit être réutilisée dans tous les instruments qui en dépendent.

### 4.4 API First

Toutes les fonctions importantes doivent être exposables via des APIs documentées.

### 4.5 Auditability by Design

Toute opération significative doit être traçable : auteur, date, objet, état antérieur, état nouveau, justification et workflow.

### 4.6 Human-in-the-Loop

L'IA propose ; l'autorité compétente valide lorsque la décision a une portée administrative, financière, juridique ou institutionnelle.

### 4.7 Modular Monolith First

Le cœur métier initial sera un **monolithe modulaire** fortement structuré. Les microservices ne seront extraits que lorsque la volumétrie, l'autonomie de déploiement, la sécurité ou le scaling le justifient.

### 4.8 Open Standards et portabilité

BIE doit rester portable entre cloud public, cloud gouvernemental, datacenter national et architecture hybride.

## 5. Architecture en plans

```text
EXPERIENCE PLANE
        ↓
ACCESS & SECURITY PLANE
        ↓
DOMAIN CORE
        ↔
AI PLATFORM
        ↓
WORKFLOW / EVENT PLANE
        ↓
DATA & KNOWLEDGE PLANE
```

## 6. Frontend

Stack cible :

- Next.js ;
- React ;
- TypeScript ;
- TanStack Query ;
- TanStack Table ;
- React Hook Form ;
- Zod ;
- dnd-kit ;
- Apache ECharts ;
- MapLibre GL ;
- deck.gl ;
- BIE Design System.

Le frontend doit privilégier la simplicité, les tableaux de masse, le copier-coller, le drill-down, la cartographie et les interfaces contextuelles de l'IA.

## 7. Cœur métier

Stack cible :

- Kotlin ;
- Spring Boot ;
- Domain-Driven Design ;
- Modular Monolith ;
- Domain Events ;
- règles métier déterministes ;
- REST/OpenAPI ;
- gRPC lorsque justifié ;
- CQRS sélectif.

Modules principaux :

```text
StrategicPlanning
ProgrammaticManagement
OperationalPlanning
Costing
Budgeting
Procurement
PublicInvestment
Treasury
PerformanceManagement
MonitoringEvaluation
RiskManagement
TerritorialIntelligence
DocumentManagement
Administration
```

Organisation recommandée de chaque module :

```text
domain/
application/
infrastructure/
api/
```

## 8. Plateforme IA

Stack :

- Python ;
- FastAPI ;
- Pydantic ;
- PyTorch ;
- Transformers ;
- sentence-transformers ;
- scikit-learn ;
- Polars ;
- PyArrow ;
- DuckDB.

L'accès aux modèles passe par un **AI Gateway** interne afin de garantir l'indépendance vis-à-vis des fournisseurs.

## 9. Modèles locaux

Le serving local pourra être assuré par vLLM ou une solution équivalente, notamment pour :

- modèles sensibles ;
- modèles spécialisés ;
- embeddings ;
- rerankers ;
- classification ;
- extraction ;
- vision.

## 10. Structured Outputs et tools

Toute sortie consommée par du code doit être structurée et validée.

```text
LLM
 ↓
JSON Schema
 ↓
Validator
 ↓
Permissions
 ↓
Business Rules
 ↓
Command
```

## 11. Workflow durable

**Temporal** constitue le moteur privilégié pour les workflows :

- longs ;
- multi-étapes ;
- nécessitant approbation ;
- tolérant les pannes ;
- nécessitant retry/compensation.

## 12. Event streaming

**Redpanda** est retenu comme couche Kafka-compatible pour :

- domain events ;
- CDC ;
- analytics temps réel ;
- intégrations.

Le pattern **Transactional Outbox** évitera les incohérences entre transaction métier et événement publié.

## 13. Architecture des données

### PostgreSQL

Source de vérité transactionnelle.

### ClickHouse

Analytics OLAP à grande échelle.

### Qdrant

Recherche sémantique et hybride.

### Neo4j

Knowledge Graph.

### S3 compatible

Documents, pièces, exports et Lakehouse.

### Valkey/Redis

Cache et données temporaires lorsque nécessaire.

### Lakehouse

Évolution cible :

```text
S3
+
Parquet
+
Apache Iceberg
```

## 14. Identité et politique

- Keycloak : IAM / SSO / OIDC / MFA ;
- OPA : politiques d'autorisation ;
- RBAC + ABAC ;
- séparation des fonctions ;
- identités de services et d'agents.

## 15. RAG et GraphRAG

BIE ne doit pas utiliser un « RAG naïf ». La recherche doit combiner :

```text
lexical
+
dense
+
sparse
+
metadata filters
+
reranking
+
graph expansion
```

Le Knowledge Graph sera utilisé lorsqu'une question exige des relations entre stratégie, organisation, budget, activité, marché, contrat et résultat.

## 16. Mémoire

Quatre couches :

1. mémoire de session ;
2. mémoire de travail ;
3. mémoire institutionnelle ;
4. mémoire de connaissance.

Une conversation utilisateur ne modifie jamais directement la mémoire officielle.

## 17. Observabilité

Stack :

- OpenTelemetry ;
- Prometheus ;
- Grafana ;
- Loki ;
- Tempo.

L'observabilité doit couvrir les appels applicatifs et les appels IA : modèle, agent, outil, latence, tokens, coût, erreurs et validation humaine.

## 18. DevOps

- OCI containers ;
- Kubernetes ;
- OpenTofu/Terraform ;
- Helm ;
- Argo CD ;
- GitOps ;
- Vault/KMS ;
- SBOM ;
- signatures d'artefacts.

## 19. Interopérabilité

BIE doit être :

- API-first ;
- Canonical Data Model ;
- MCP-ready ;
- A2A-ready ;
- capable d'intégrer SIGFP, SYCOREF, PAG et le système officiel e-procurement via des adaptateurs.

## 20. Déploiement progressif

### MVP

```text
Next.js
Kotlin/Spring
Python/FastAPI
PostgreSQL
Qdrant
S3
Keycloak
Temporal
```

### Étape multi-structures

Ajouter :

```text
Redpanda
OPA
ClickHouse
Observability complète
```

### Étape nationale

Ajouter :

```text
Neo4j
Lakehouse
Advanced ML
Local AI cluster
National Data Platform
```

## 21. Principes non négociables

1. Le cœur métier reste indépendant des fournisseurs IA.
2. PostgreSQL est la vérité transactionnelle de BIE.
3. Le LLM ne remplace jamais le Rules Engine.
4. Les sorties machine de l'IA sont structurées.
5. Les agents appliquent le moindre privilège.
6. Les opérations critiques sont auditées.
7. Les données officielles importantes sont versionnées.
8. Les réponses documentaires sont citables.
9. L'analytique massive ne doit pas dégrader l'OLTP.
10. L'architecture commence modulaire.
11. Aucun verrouillage cloud n'est obligatoire.
12. L'infrastructure est reproductible.
13. Les politiques d'autorisation sont centralisables.
14. La classification des données conditionne le routage IA.
15. Tout changement de modèle important passe par des evals.
16. L'observabilité couvre l'IA.
17. Les workflows longs sont durables.
18. L'interopérabilité est prévue dès le départ.
19. L'architecture peut évoluer vers un déploiement souverain.
20. **Complexité dans le moteur, simplicité dans l'interface.**

## 22. Positionnement final

```text
BIE
├── Public Finance Transaction Platform
├── Budget Rules Engine
├── Planning & Performance Platform
├── Public Finance Data Platform
├── Knowledge Platform
├── AI Agent Platform
├── Analytics Platform
└── Integration Platform
```

La doctrine finale est :

> **Les moteurs métier déterministes constituent l'autorité ; les données constituent la preuve ; l'intelligence artificielle constitue l'assistance.**
