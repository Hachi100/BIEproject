---
title: "Partie 7 — Data & Knowledge Platform"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 7 — Data & Knowledge Platform

## Lakehouse, Document Intelligence, Hybrid RAG, Knowledge Graph et mémoire nationale

## 1. Objet

Cette partie définit la plateforme de données et de connaissance servant de fondation aux modules métier, analytics et IA.

## 2. Architecture

```text
SOURCES
 ↓
INGESTION
 ↓
RAW
 ↓
STANDARDIZED
 ↓
CURATED
 ↓
PostgreSQL / Lakehouse / ClickHouse
 ↓
Qdrant / Neo4j / Metadata
 ↓
Search / RAG / GraphRAG
```

## 3. Distinction essentielle

```text
DATA
DOCUMENT
KNOWLEDGE
```

sont trois objets distincts mais reliés.

## 4. PostgreSQL

Source de vérité transactionnelle.

## 5. ClickHouse

Serving analytique haute performance.

## 6. Object Storage

S3-compatible pour :

- documents ;
- Lakehouse ;
- exports ;
- archives.

## 7. Qdrant

Recherche dense/sparse/hybride.

## 8. Neo4j

Knowledge Graph métier.

## 9. Lakehouse

Stack :

```text
S3
+
Parquet
+
Apache Iceberg
+
open catalog
```

## 10. Zones

```text
LANDING
RAW
STANDARDIZED
CURATED
DATA PRODUCTS
```

## 11. Raw Immutable

La donnée originale n'est pas détruite par les transformations.

## 12. Metadata First

Tout actif doit disposer :

```text
source
owner
schema
classification
retention
```

## 13. Data Catalog

Le catalogue doit permettre :

- discovery ;
- owners ;
- glossaire ;
- lineage ;
- qualité ;
- certification.

## 14. Business Glossary

Termes :

```text
AE
CP
DPPD
PAP
PTAB
PPM
PCC
RAP
RAPEX
```

avec définition officielle.

## 15. Data Ownership

Chaque domaine a :

```text
Data Owner
Data Steward
```

## 16. Data Contracts

Chaque flux important précise :

```text
schema
semantics
quality
freshness
security
version
```

## 17. Schema Registry

Pour les événements et APIs structurées.

## 18. Data Lineage

Répondre :

> D'où vient ce chiffre ?

## 19. Technical Lineage

```text
Source
 ↓
Pipeline
 ↓
Table
 ↓
Metric
 ↓
Dashboard
```

## 20. Business Lineage

```text
PTAB
 ↓
PPM
 ↓
Contract
 ↓
Payment
```

## 21. Data Quality

Dimensions :

```text
Completeness
Validity
Accuracy
Consistency
Uniqueness
Timeliness
Integrity
```

## 22. Quality Incident

Objet avec :

```text
dataset
rule
severity
owner
status
```

## 23. Ingestion Modes

```text
API
CDC
EVENT
BATCH
FILE
DOCUMENT
```

## 24. Excel Intelligence

Pipeline :

```text
upload
 ↓
detect sheets
 ↓
detect columns
 ↓
mapping
 ↓
preview
 ↓
validation
 ↓
import
```

## 25. Quarantine

Les lignes invalides sont isolées, expliquées et retraitables.

## 26. Document Intelligence

Documents :

- DPPD ;
- PAP ;
- PTAB ;
- PPM ;
- RAP ;
- RAPEX ;
- lois ;
- décrets ;
- contrats ;
- études.

## 27. Document Pipeline

```text
Security scan
 ↓
Native extraction
 ↓
Layout
 ↓
Tables
 ↓
OCR if required
 ↓
Structured extraction
 ↓
Validation
 ↓
Indexing
```

## 28. Layout-Aware

Conserver :

```text
page
heading
paragraph
table
figure
coordinates
```

## 29. Citation Anchors

Chaque passage indexé porte :

```text
document_id
page
section
bounding_box
```

## 30. Versioning

```text
Draft
Validated
Revised
Superseded
```

avec hash.

## 31. Chunking

BIE utilise :

- structural chunking ;
- semantic chunking ;
- parent-child retrieval ;
- table-aware chunks ;
- article-aware chunks.

## 32. Structured Extraction

Exemple :

```text
DPPD
 ↓
Programs[]
Actions[]
Indicators[]
Targets[]
```

Chaque champ critique peut porter une confiance et une provenance.

## 33. Unified Search

Modes :

```text
EXACT
LEXICAL
SEMANTIC
FILTERED
GRAPH
SQL
```

## 34. Hybrid Retrieval

```text
Query
 ├── Dense
 ├── Sparse
 ├── Metadata
 ↓
Fusion
 ↓
Reranker
```

## 35. Security Filtering

La permission est appliquée avant de retourner les résultats.

## 36. RAG Router

```text
Question
 ↓
Intent
 ├── SQL
 ├── Documents
 ├── Graph
 ├── Rules
 └── Hybrid
```

## 37. Structured Data First

Pour les valeurs officielles structurées, utiliser SQL/API plutôt qu'un PDF.

## 38. Regulatory RAG

La recherche doit filtrer selon :

```text
effective date
official status
jurisdiction
```

## 39. Knowledge Graph

Nœuds :

```text
Vision
Objective
Policy
Organization
Program
Action
Activity
Task
Budget
Project
Procurement
Contract
Indicator
Document
Evaluation
```

## 40. Official vs Inferred

Relations :

```text
OFFICIAL
INFERRED
```

toujours distinguées.

## 41. Entity Resolution

Résoudre :

```text
MCVT
Ministère du Cadre de Vie
Ministère du Cadre de Vie et des Transports
```

vers une entité canonique lorsque pertinent.

## 42. Knowledge Provenance

Chaque relation extraite conserve :

```text
source
page
extractor
confidence
```

## 43. Contradictions

Les valeurs divergentes ne sont jamais silencieusement écrasées.

## 44. Temporal Retrieval

Question :

> Quelle règle était applicable en 2027 ?

Le système sélectionne la version valide à cette date.

## 45. Memory Layers

```text
National
Institutional
Domain
Session
```

## 46. Knowledge Promotion

```text
PROPOSED
 ↓
REVIEWED
 ↓
VALIDATED
 ↓
OFFICIAL
```

## 47. Data Security

Contrôles :

```text
tenant
row
column
document
chunk
```

## 48. Encryption

Données chiffrées au repos et en transit.

## 49. Time Travel

Le Lakehouse doit permettre de reconstruire des états historiques.

## 50. Reproducibility

Un rapport historique doit rester reproductible.

## 51. AI Dataset Registry

Les datasets de training/evaluation sont versionnés et traçables.

## 52. Retrieval Eval

Mesures :

```text
Recall@K
MRR
nDCG
Precision
```

## 53. Data Governance

Organisation :

```text
Domain Owner
Data Steward
Technical Custodian
Security
```

## 54. Certified Data

Statuts :

```text
CERTIFIED
VERIFIED
UNVERIFIED
DEPRECATED
```

## 55. Historical Backfill

Importer progressivement :

- données récentes ;
- anciens PTAB ;
- DPPD ;
- PPM ;
- RAP/RAPEX ;
- textes ;
- prix.

## 56. Requirements

| ID | Exigence |
|---|---|
| DATA-001 | System of Record |
| DATA-002 | Lakehouse |
| DATA-003 | analytics |
| DATA-004 | vector search |
| DATA-005 | Knowledge Graph |
| DATA-006 | metadata |
| DATA-007 | lineage |
| DATA-008 | quality |
| DATA-009 | provenance |
| DATA-010 | historical memory |

## 57. Conclusion

La Data & Knowledge Platform doit faire de BIE une mémoire institutionnelle capable de retrouver, expliquer et relier les connaissances publiques sur plusieurs exercices, sans confondre données officielles, estimations et inférences.
