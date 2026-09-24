# Budget Intelligence Engine (BIE)

> **« De la Vision nationale au franc programmé ; du franc dépensé au résultat obtenu. »**

BIE est la plateforme nationale d'intelligence des finances publiques et de la performance du Bénin. Elle relie
stratégie, PTAB, costing, budget, passation, exécution et performance dans un modèle de données unique, en
complément des systèmes officiels (SIGFP, SYCOREF, e-Procurement).

## Cahier des charges

- [Cahier des charges maître](./Cahier_des_charges_Maitre_BIE.md)
- Parties : [1 Vision et architecture](./Partie_01_BIE.md) · [2 Modèle canonique](./Partie_02_BIE.md) ·
  [3 Planification](./Partie_03_BIE.md) · [4 Costing, budget, PCC](./Partie_04_BIE.md) ·
  [5 Passation et investissements](./Partie_05_BIE.md) · [6 Suivi et performance](./Partie_06_BIE.md) ·
  [7 Données et connaissances](./Partie_07_BIE.md) · [8 AI OS](./Partie_08_BIE.md) ·
  [9 Sécurité](./Partie_09_BIE.md) · [10 UX, déploiement, feuille de route](./Partie_10_BIE.md)
- [Sources officielles exploitées](./docs/sources/README.md) : catalogue de 34 documents de la DGB et de la DNCF,
  enseignements traduits en règles.
- [ADR 0001 : architecture de l'incrément 1](./docs/adr/0001-architecture-increment-1.md)

## Incrément 1 : ce qui fonctionne

| Domaine | Livré | Exigences |
|---|---|---|
| Référentiel | 36 institutions actives, 107 programmes et dotations de la **LF 2026**, historisés par période de validité ; crédits 2022-2028 | REF-001..003, MOD-001 |
| PTAB | Création versionnée par institution et exercice ; activités rattachées à un programme actif ; tâches avec dépendances | PLN-001, PLN-003..005 |
| Costing | Ligne = quantité × fréquence × prix unitaire ; **contrôle contre l'e-Répertoire v26.3** (10 093 articles) : BS bloquante, BI en alerte, homologation CERPR hors répertoire, avis DSI/DGML, imputation suggérée par le code article ; provenance du prix obligatoire | CST-001..005 |
| Enveloppes | Plafonds **LF 2026** par programme et catégorie économique ; besoin vs disponible ; révision de plafond versionnée et justifiée | BUD-001, BUD-005, BUD-007 |
| Workflow | Brouillon → soumis → en revue → validé → approuvé, renvoi motivé ; **préparer ≠ contrôler ≠ approuver** ; contrôles bloquants avant soumission | WF-001..005 |
| Audit | Journal **inaltérable** chaîné par SHA-256, écrit dans la transaction métier ; vérification d'intégrité | SEC-007, AI-010 |
| Import PTAB | Lecture de PTAB Excel/CSV hétérogènes : en-têtes, colonnes, hiérarchie, montants français, chronogrammes, rapprochement des totaux, **zéro perte silencieuse** | PLN-001, DATA-008 |

Pas encore livré : IAM Keycloak, frontend, PPM, PCC, RAG documentaire, Copilot. Voir « Prochaines étapes ».

## Structure du dépôt

```text
backend/        Cœur métier — Kotlin 2.4, Spring Boot 4.1, PostgreSQL 16, Flyway (monolithe modulaire)
ai-platform/    Plateforme IA — Python 3.11, FastAPI : import intelligent des PTAB
infra/          docker compose (PostgreSQL ; Keycloak, Qdrant, MinIO, Temporal avec le profil « complet »)
scripts/        Extraction des référentiels officiels, téléchargement des sources
docs/           Sources officielles, décisions d'architecture (ADR)
```

## Démarrage rapide

Prérequis : Java 21, Docker, [uv](https://docs.astral.sh/uv/).

```bash
# 1. Base de données
docker compose -f infra/docker-compose.yml up -d postgres

# 2. Cœur métier (http://localhost:8080, documentation API : /api/docs)
cd backend && ./gradlew bootRun
#   Au premier démarrage, Flyway charge les référentiels officiels (programmes LF 2026, e-Répertoire v26.3).

# 3. Plateforme IA (http://localhost:8000/docs)
cd ai-platform && uv sync && uv run uvicorn bie_ai.main:app --reload
```

### Scénario de démonstration (API)

```bash
H='-H X-BIE-User:preparateur -H X-BIE-Tenant:MS -H Content-Type:application/json'
# Créer le PTAB 2026 du ministère de la Santé
curl -s $H -d '{"fiscalYear":2026}' localhost:8080/api/v1/ptabs
# Ajouter une activité au programme 045, une tâche, puis une ligne de coût contrôlée par le répertoire :
curl -s $H -d '{"label":"Revue du PTA","programCode":"045","responsibleUnit":"DPAF","startDate":"2026-06-01","endDate":"2026-07-31","executionMode":"DIRECT"}' localhost:8080/api/v1/ptabs/<ptab>/activities
curl -s $H -d '{"label":"Atelier de revue","responsible":"SPSE","startDate":"2026-07-01","endDate":"2026-07-03"}' localhost:8080/api/v1/activities/<activité>/tasks
curl -s $H -d '{"description":"Scotch adhésif","resourceNature":"SUPPLIES","quantity":40,"unit":"U","unitPrice":1500,"priceSource":"PRICE_REPERTOIRE","priceReferenceCode":"6013 3321 251 1111"}' localhost:8080/api/v1/tasks/<tâche>/costing-lines
#   → controlStatus = BLOCKING : 1 500 FCFA > BS 1 250 FCFA
# Contrôles, enveloppes, soumission
curl -s $H localhost:8080/api/v1/ptabs/<ptab>/submission-check
curl -s $H localhost:8080/api/v1/ptabs/<ptab>/envelope-check
curl -s $H -d '{"action":"SUBMIT"}' localhost:8080/api/v1/ptabs/<ptab>/transitions
```

### Tester l'import sur vos PTAB

```bash
cd ai-platform
uv run bie-import-ptab ~/PTAB/PTAB_2026_MS.xlsx ~/PTAB/PTA_MEF_2025.xls --json rapport.json
```

Pour chaque fichier, la commande affiche :

- les colonnes reconnues ;
- la structure lue (programmes → actions → activités → tâches) ;
- les lignes en quarantaine et leur motif ;
- les écarts entre les totaux déclarés et les montants détaillés.

## Tests

```bash
cd backend && LC_ALL=C.UTF-8 ./gradlew test                        # 39 tests, dont intégration PostgreSQL (Docker requis)
cd ai-platform && uv run ruff check src tests && uv run pytest     # 70 tests
```

## Prochaines étapes proposées

1. **Calibrer l'import sur les PTAB réels des ministères**, puis créer un PTAB dans le cœur métier à partir d'un aperçu
   validé.
2. IAM Keycloak (OIDC, MFA), rôles RFFiM / R-Prog / RA / RAc / DCF, Row-Level Security.
3. Frontend Next.js : espace de travail, tableau PTAB en masse, aperçu d'import.
4. PPM dérivé du PTAB (besoins externes, seuils versionnés) et PCC dérivé du calendrier.
5. Pipeline documentaire (OCR des textes numérisés) et RAG citant ses sources.
