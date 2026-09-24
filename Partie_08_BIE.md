---
title: "Partie 8 — BIE AI Operating System"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 8 — BIE AI Operating System

## Budget Copilot, orchestration multi-agents, model routing, tools, mémoire, guardrails et evals

## 1. Objet

BIE AI OS est la couche cognitive transversale de la plateforme.

Elle comprend :

```text
Budget Copilot
Intent Router
Context Engine
Model Router
Agent Orchestrator
Tool Registry
Memory
Evals
Guardrails
FinOps
```

## 2. Hiérarchie d'autorité

```text
LAW
 ↓
OFFICIAL REFERENCES
 ↓
BUSINESS RULES
 ↓
AUTHORIZATION
 ↓
VALIDATED DATA
 ↓
AI
```

## 3. Autonomie

Niveaux :

```text
L0 Inform
L1 Suggest
L2 Prepare
L3 Execute after approval
L4 Limited automatic execution
```

Actions critiques restent humaines.

## 4. AI Control Plane

Décide :

```text
what
agent
model
tools
context
permissions
cost
```

## 5. Provider Independence

```text
BIE
 ↓
AI Gateway
 ├── Cloud provider
 ├── Cloud provider
 └── Local vLLM
```

## 6. Model Registry

```text
provider
model_id
capabilities
security_policy
cost
latency
status
```

## 7. Model Routing

Critères :

```text
capability
complexity
confidentiality
cost
latency
quality
```

## 8. Local Models

Cas d'usage :

- extraction ;
- classification ;
- embeddings ;
- reranking ;
- données sensibles ;
- tâches répétitives.

## 9. Cascade Routing

```text
small
 ↓ if insufficient
medium
 ↓
advanced
```

## 10. Budget Copilot

Accessible globalement et contextuellement.

Modes :

```text
ASK
ANALYZE
CREATE
SIMULATE
ACT
```

## 11. Context Engineering

```text
Request
 ↓
Intent
 ↓
Context plan
 ↓
Retrieve necessary evidence only
 ↓
Model
```

## 12. Context Priority

```text
Security
System policy
Business rules
Official data
Evidence
User context
```

## 13. Memory

```text
Session
Work
Institutional
Knowledge
```

Une conversation ne devient pas mémoire officielle automatiquement.

## 14. Intent Router

Intents :

```text
QUESTION
SEARCH
ANALYSIS
SIMULATION
CREATE_DRAFT
MODIFY
EXECUTE
EXPLAIN
```

## 15. Domain Router

```text
PLANNING
COSTING
BUDGET
PROCUREMENT
INVESTMENT
PERFORMANCE
DATA
```

## 16. Risk Classification

```text
READ_ONLY
LOW_RISK_WRITE
CONTROLLED_WRITE
CRITICAL
```

## 17. Agent Orchestrator

Pipeline :

1. analyser ;
2. planifier ;
3. choisir agents ;
4. choisir outils ;
5. exécuter ;
6. vérifier ;
7. consolider ;
8. demander approbation si nécessaire.

## 18. Deterministic Outer Loop

Le workflow est contrôlé par code/Temporal, pas par un LLM libre.

## 19. Durable Execution

Temporal permet :

- retries ;
- attentes humaines ;
- reprise après panne ;
- compensation.

## 20. Specialist Agents

```text
Planning Agent
Costing Agent
Budget Agent
Procurement Agent
Investment Agent
Performance Agent
Risk Agent
Data Agent
Knowledge Agent
Document Agent
Simulation Agent
Verification Agent
```

## 21. Agent Scope

Chaque agent voit uniquement les outils nécessaires.

## 22. Tool Registry

```text
name
description
input_schema
output_schema
risk_level
permissions
approval_policy
```

## 23. No Direct DB Writes

```text
Agent
 ↓
Tool
 ↓
Application Service
 ↓
Rules
 ↓
Database
```

## 24. Structured Outputs

Toute action machine utilise JSON Schema / Pydantic.

## 25. Proposed Command

L'IA produit un objet proposé, jamais directement une mutation non contrôlée.

## 26. AI Action Gateway

```text
AI Output
 ↓
Schema
 ↓
Permission
 ↓
Rules
 ↓
Approval
 ↓
Domain Command
```

## 27. Tool Calling

Les valeurs transactionnelles sont obtenues par outils.

L'IA ne devine jamais un reliquat budgétaire.

## 28. MCP

BIE sera MCP-ready pour exposer des tools/resources standardisés.

MCP ne remplace pas IAM/OPA.

## 29. A2A

BIE sera A2A-ready pour les interactions entre agents indépendants.

## 30. Trust Levels

```text
INTERNAL_TRUSTED
INTERNAL_LIMITED
PARTNER
EXTERNAL
UNTRUSTED
```

## 31. Multi-Agent Patterns

- router ;
- supervisor ;
- plan-and-execute ;
- map-reduce ;
- hierarchical ;
- debate uniquement si utile.

## 32. AgentRun

```text
run_id
user
agent
task
model
tools
status
cost
```

## 33. Traces

Enregistrer :

- décisions ;
- outils ;
- sources ;
- validations ;
- résultats.

Pas besoin de stocker un raisonnement interne libre du modèle.

## 34. Verification Agent

Contrôle :

- sources ;
- cohérence ;
- règles ;
- claims non supportés.

## 35. External Web Content

Toujours considéré comme non fiable.

## 36. Prompt Injection Protection

Les documents/pages sont du contenu, jamais des instructions système.

## 37. Threats

```text
prompt injection
tool abuse
excessive agency
exfiltration
memory poisoning
RAG poisoning
resource exhaustion
```

## 38. Sandbox

Code/analysis dans environnement isolé.

## 39. Safe SQL

Pipeline :

```text
LLM SQL
 ↓
Parser
 ↓
Read-only validation
 ↓
Policy
 ↓
Execution
```

## 40. Effective Permission

```text
User
∩ Agent
∩ Tool
∩ Policy
```

## 41. Action Risk Levels

```text
R0 Read
R1 Draft
R2 Reversible write
R3 Workflow transition
R4 Critical action
```

## 42. Preview Before Apply

Avant toute action sensible, afficher :

```text
before
after
impact
```

## 43. Idempotency

Les retries ne doivent pas dupliquer une action.

## 44. Cost Controls

Chaque run :

```text
max_tokens
max_steps
max_tools
max_cost
max_duration
```

## 45. Loop Protection

Détection de répétitions outils/arguments.

## 46. Prompt Registry

Prompts versionnés, évalués, rollbackables.

## 47. Eval-Driven Development

Couches :

```text
model
prompt
retrieval
tool
agent
workflow
safety
business
```

## 48. Golden Datasets

Validés par experts.

## 49. Metrics

```text
accuracy
recall
citation correctness
tool selection
human acceptance
latency
cost
```

## 50. Shadow / Canary

Nouveaux modèles testés sans impact immédiat, puis déployés progressivement.

## 51. Observability

OpenTelemetry sur :

```text
router
agent
model
tool
retrieval
verifier
```

## 52. AI FinOps

Mesurer :

```text
organization
agent
task
model
cost
```

## 53. UX

L'IA apparaît :

- inline ;
- side panel ;
- command palette ;
- insights ;
- chat.

## 54. Proactive AI

Les insights automatiques doivent être actionnables et priorisés.

## 55. Simulation

Le LLM comprend et explique ; un solver calcule.

## 56. Document Generation

```text
Structured data
+
Template
+
Narrative AI
=
Document
```

Les chiffres viennent des données.

## 57. Feedback

```text
Accept
Edit
Reject
```

Les corrections alimentent un dataset, mais pas un fine-tuning automatique.

## 58. Fine-Tuning

Utiliser pour tâches stables :

- classification ;
- format ;
- extraction ;
- langage métier.

Les règles changeantes restent dans RAG + Rules.

## 59. Sovereign AI

À long terme, modèles spécialisés/distillés locaux.

## 60. AI-Off Mode

BIE doit continuer à fonctionner sans LLM.

## 61. Requirements

| ID | Exigence |
|---|---|
| AI-001 | Copilot |
| AI-002 | model gateway |
| AI-003 | model routing |
| AI-004 | agents |
| AI-005 | tools structurés |
| AI-006 | durable workflows |
| AI-007 | RAG / GraphRAG |
| AI-008 | human approval |
| AI-009 | evals |
| AI-010 | audit / observability |

## 62. Conclusion

BIE AI OS n'est pas un bouton « Demander à l'IA ». C'est un système de contrôle qui combine modèles, données, règles, tools, workflows et validation humaine afin d'augmenter l'administration sans déplacer la responsabilité publique vers un modèle probabiliste.
