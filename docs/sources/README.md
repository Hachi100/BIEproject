# Sources officielles utilisées par BIE

Ce dossier recense les documents publics sur lesquels s'appuie le développement de BIE, et ce que chacun
apporte au produit. Recherche menée le 24 septembre 2026 sur :

- **budgetbenin.bj**, le site de la Direction Générale du Budget (DGB). Il publie environ 7 200 médias, dont
  3 100 PDF et 43 classeurs Excel, inventoriés par son API WordPress.
- **erepertoire.finances.bj**, la plateforme e-Répertoire des prix de référence de la DNCF.
- **finances.bj**, le site du MEF, **suspendu** à la date de la recherche (page d'hébergeur).

Le catalogue complet, lisible par machine, est dans [`catalogue.csv`](./catalogue.csv) : 34 documents, liens vérifiés.
Pour reconstituer le corpus en local (dossier `data/`, non versionné) :

```bash
python scripts/sources/telecharger_sources.py                   # tout le catalogue
python scripts/sources/telecharger_sources.py --id pap_2026     # un document
```

## Données déjà intégrées au produit

| Source | Contenu intégré | Où |
|---|---|---|
| Classifications croisées 2022-2028, budget LF 2026 (DGB, xlsx) | 36 institutions actives en 2026, 107 programmes et dotations, 2 246 lignes de crédits par nature économique, enveloppes 2026 | migrations `V5`, `V7` du backend |
| e-Répertoire des prix de référence, 19e édition v26.3 (DNCF, juin 2026) | 10 093 articles, bornes BI/BS, code arrimé NBE/PCE/PCME, spécifications | migration `V6` du backend |

Les scripts d'extraction sont dans [`scripts/referentiel/`](../../scripts/referentiel). Ils sont rejouables à chaque nouvelle
publication (LF 2027, e-Répertoire v26.4…).

## Ce que les documents apprennent au produit

### 1. Référentiel programmatique : les codes ne sont pas des identifiants stables

La LF 2026 couvre la période 2022-2028. On y lit les restructurations ministérielles :

- **MIT** (Infrastructures et Transports) et **ME** (Énergie) portent des crédits jusqu'en 2023 ;
- leurs programmes passent ensuite au **MCVT** et au **MEEM**.

Des codes sont **réutilisés** :

- `078` = pilotage du ME, puis Mines au MEEM ;
- `084` = forêts au MCVT, puis espaces frontaliers au MISP ;
- `010` = deux dotations.

Certains codes sont aussi publiés sans normalisation (`31`, `0113`).

Conséquence dans BIE : un programme est identifié par son code **et sa période de validité**
(`valid_from_year` / `valid_to_year`). Les anomalies de la source sont conservées dans `quality_flags`, jamais corrigées
en silence.

### 2. Costing : les règles du répertoire des prix sont des règles de contrôle

D'après le guide d'utilisation de l'e-Répertoire (19e édition) :

- les utilisateurs **ne sont pas autorisés à dépasser la borne supérieure (BS)**. Dans BIE, une ligne de coût au-dessus de
  la BS est **bloquante** et empêche la soumission du PTAB ;
- les prix sont **TTC** pour les produits soumis à la TVA, et **HT = TTC / 1,18** ;
- depuis la 19e édition, la **borne inférieure ne descend jamais sous le prix minimum collecté** : les bornes ne sont plus
  un simple ±25 % de la médiane (c'était le cas en 2020). BIE stocke donc les bornes publiées et ne les recalcule pas ;
- un **article hors répertoire** exige l'avis préalable de la **CERPR**, via une demande d'homologation sur e-Répertoire,
  normalement au **4e trimestre de N pour N+1** (phase pré-budgétaire du PTA). BIE marque la ligne « homologation
  requise » ;
- **matériel informatique et mobilier** : avis préalables du DSI et de la DGML selon le profil de l'utilisateur ;
- le **code article est arrimé à la nomenclature budgétaire** : classe 2 = immobilisation, classe 6 = charge. BIE s'en sert
  pour **suggérer** l'imputation économique et trace que la valeur est déduite ;
- le répertoire est la base de **SYCOREF** (système de costing de la DNCF), ce qui confirme le positionnement de BIE :
  s'intégrer à SYCOREF, pas le dupliquer.

Défauts relevés dans la v26.3 publiée (à signaler à la DNCF) :

- 11 cellules affichent « ####### » (colonne Excel trop étroite à l'export PDF) ;
- 52 articles sans BI, surtout des accords-cadres informatiques à prix unique ;
- le SUV Honda CR-V a une BS tronquée à « 32 ».

BIE signale ces articles au lieu d'appliquer une borne douteuse.

### 3. Planification : acteurs, calendrier et formats réels

- **Acteurs du budget-programme** (manuel de préparation) : RFFiM, Responsable de programme (R-Prog), Responsable d'action
  (RA), Responsable d'activité (RAc), Délégué du Contrôleur financier (DCF), DPP/DAF. Ce sont les futurs rôles de
  `RoleAssignment`.
- **Calendrier budgétaire** : un arrêté annuel fixe le planning (n° d'étape, tâche, période, structure responsable,
  structures associées). C'est exactement l'objet `BudgetCalendar` de la Partie 2 §26. L'arrêté 2027-2029 est un PDF
  numérisé : sa reprise demandera une OCR relue.
- **PAP 2026** (édités par SIGFP v1.0) : programme (numéro, responsable), actions et structures de mise en œuvre,
  objectifs spécifiques, indicateurs (unité, valeur de référence, historique sur 3 ans, cibles sur 3 ans, structure de
  publication, source, méthode de calcul). SIGFP produit déjà DPPD et PAP : BIE doit s'y connecter.
- **PTA réel (DCFP 2021)** : activité → tâches, avec période d'exécution, personne responsable et observations.
- **Revue de PTA (MASM 2024)** : indicateurs suivis en pratique, à savoir taux d'exécution physique, taux d'engagement,
  taux d'ordonnancement et état des recommandations (exécutées, en cours, non exécutées).

### 4. Nomenclature économique retenue

Les enveloppes suivent la présentation de la loi de finances :

- dépenses de **personnel**, **acquisitions de biens et services**, **transferts** ;
- dépenses en **capital** ventilées par ressource : intérieures, dons, prêts.

Cela correspond aux titres de la nomenclature UEMOA (directive n°08/2009).

## Points d'attention

- Plusieurs textes majeurs ne sont publiés qu'en **PDF numérisé** : LOLF, décret 2024-854, lettre de cadrage 2027-2029,
  calendrier 2027-2029. Il faudra les passer en OCR avec relecture humaine (pipeline documentaire, Partie 7 §27) avant tout
  usage par le RAG.
- Aucun **canevas officiel de PTAB** n'est publié sur budgetbenin.bj. Le moteur d'import de BIE se calibre donc sur les PTAB
  réels des ministères (voir `ai-platform/`).
