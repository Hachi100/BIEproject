# BIE : plateforme IA

Services Python de Budget Intelligence Engine. Incrément 1 : **import intelligent des PTAB** (Partie 7 §24 « Excel
Intelligence »).

## Import des PTAB

Les ministères produisent leurs PTA/PTAB dans des canevas Excel hétérogènes. Le moteur `bie_ai.ptab_import` :

1. **lit** les fichiers `.xlsx`, `.xls` et `.csv` (séparateur `;` ou `,`, encodages UTF-8 et Windows), en conservant les cellules fusionnées ;
2. **repère la ligne d'en-tête**, y compris sur deux lignes (« Chronogramme » au-dessus de T1…T4, « Financement »
   au-dessus de BN/FINEX), et choisit la feuille la plus proche d'un PTAB ;
3. **rapproche les colonnes** des champs canoniques par synonymes exacts, puis par correspondance approchée
   ([`synonymes.json`](src/bie_ai/ptab_import/synonymes.json), à enrichir avec les canevas réels) ;
4. **reconstitue la hiérarchie** programme → action → activité → tâche :
   - colonnes explicites, avec propagation des cellules fusionnées ;
   - ou libellé unique et codes hiérarchiques (`026.1.01.2`), niveaux déduits de la profondeur des codes ;
   - ou préfixes (« Action 2 : … ») ;
5. **lit les valeurs** :
   - montants au format français (`1 250 000`, `1.250.000`, `3 250 000,50`, `1,5 M`) et unité déclarée (« en milliers
     de FCFA ») ;
   - dates, trimestres, mois, périodes en texte (« T1 - T3 », « Mars à juin ») ;
   - financement (BN, FINEX, dons, prêts, bailleurs) et mode d'exécution (régie → DIRECT, marché → INDIRECT) ;
6. **contrôle et rapproche** :
   - quantité × prix unitaire ;
   - somme des financements ;
   - activité ↔ somme de ses tâches, programme ou action ↔ somme de ses activités ;
   - sous-totaux et total général ;
7. **rend compte de chaque ligne** : importée, importée avec avertissements, ignorée (titre, en-tête, total, vide) ou
   en quarantaine, toujours avec un motif. C'est le principe **zéro perte silencieuse**.

Un montant fusionné sur plusieurs lignes de tâches est rattaché à l'activité et compté **une seule fois**. Une erreur
Excel (`#REF!`), un « PM » ou « à déterminer » ne produit **jamais** de valeur inventée.

### Utilisation

```bash
uv sync
uv run bie-import-ptab PTAB_2026.xlsx --json rapport.json      # en ligne de commande
uv run uvicorn bie_ai.main:app --reload                         # API : POST /v1/ptab/import/preview
```

### Enrichir avec les canevas réels

Pour chaque nouveau canevas de ministère :

1. lancer `bie-import-ptab` sur le fichier et regarder les colonnes non reconnues et les lignes en quarantaine ;
2. ajouter les intitulés manquants dans `synonymes.json` ;
3. ajouter un cas de test dans `tests/`, sur un extrait anonymisé si nécessaire, pour figer l'interprétation.

## Développement

```bash
uv run ruff check src tests && uv run ruff format --check src tests
uv run pytest
```
