# ADR 0001 — Architecture de l'incrément 1

- Statut : accepté
- Date : 2026-09-24

## Contexte

Le cahier des charges (Parties 1, 10) prévoit un monolithe modulaire Kotlin/Spring (cœur métier), une plateforme IA en
Python/FastAPI et un frontend Next.js, sur PostgreSQL. Le MVP (Partie 10 §51) commence par l'IAM, les organisations, le
référentiel programmatique, le PTAB, le costing, les contrôles d'enveloppe et l'audit. Les ministères disposent de très
nombreux PTAB au format Excel.

## Décisions

1. **Monorepo** : `backend/` (Kotlin 2.4, Spring Boot 4.1, Java 21, Gradle 9), `ai-platform/` (Python 3.11, FastAPI, uv),
   `infra/` (docker compose), `docs/`, `scripts/`.
2. **Monolithe modulaire** : un package par domaine (`referential`, `planning`, `costing`, `budget`, `audit`) et **un schéma
   PostgreSQL par domaine**. Les écritures restent dans leur module. Seules les vues de consolidation (besoin vs
   enveloppe) lisent plusieurs schémas.
3. **Accès aux données en SQL explicite** (`JdbcClient`) plutôt qu'un ORM. Les requêtes sont lisibles, auditables et
   exploitent PostgreSQL : recherche plein texte en français, tableaux, JSONB, empreintes SHA-256.
4. **Identifiants du code en anglais**, alignés sur le modèle canonique de la Partie 2 (`OperationalActivity`, `Task`,
   `CostingLine`, `BudgetEnvelope`). **Messages, règles et documentation en français** pour les utilisateurs et les
   équipes métier.
5. **Référentiels officiels chargés par migrations Flyway** à partir des publications de la DGB et de la DNCF (Data Once).
   Les extractions sont rejouables (`scripts/referentiel/`) et les anomalies de source sont conservées dans
   `quality_flags`.
6. **Règles d'abord** : contrôle des prix, workflow et contrôles de soumission sont des fonctions pures, testées
   unitairement. Aucune décision n'est déléguée à un modèle de langage.
7. **Audit inaltérable** dans la même transaction que la modification. Une chaîne SHA-256 par institution est calculée par
   PostgreSQL, et des déclencheurs refusent `UPDATE`, `DELETE` et `TRUNCATE`. Les écritures d'une chaîne sont
   sérialisées par verrou consultatif ; au-delà de quelques centaines d'écritures par seconde par institution, on passera à
   des chaînes par lot.
8. **Isolation des institutions** : `tenant_id` sur toutes les données d'une institution, filtré dans chaque requête.
   La Row-Level Security PostgreSQL sera ajoutée en défense en profondeur.
9. **Identité provisoire** : en-têtes `X-BIE-User` et `X-BIE-Tenant`. Les services ne dépendent que de l'abstraction
   `Caller`, qu'un jeton Keycloak (OIDC) remplacera à l'incrément 2 **sans modifier le code métier**. Cette identité
   provisoire **ne doit pas être exposée hors d'un réseau de développement**.
10. **Import des PTAB déterministe** côté plateforme IA : dictionnaire de synonymes, correspondance approchée, rapport
    ligne à ligne, zéro perte silencieuse. L'IA générative n'interviendra que pour **proposer** des correspondances de
    colonnes inconnues, soumises à validation.

## Conséquences

- La suite de tests backend démarre un vrai PostgreSQL (Testcontainers) : Docker est requis pour `./gradlew test`.
- Les noms de tests contiennent des accents : lancer Gradle avec une locale UTF-8 (`LC_ALL=C.UTF-8`), faute de quoi
  l'écriture des rapports de test échoue.
- La prochaine étape de l'import est la création d'un PTAB dans le backend à partir d'un aperçu validé. Il faudra un
  endpoint d'import en masse côté backend, qui rejouera les mêmes contrôles que la saisie.
