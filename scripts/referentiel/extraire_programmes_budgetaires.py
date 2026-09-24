"""Extraction des programmes budgétaires et de leurs crédits pluriannuels.

Source : « Tableaux de classifications croisées des dépenses de l'État sur la période
pluriannuelle 2022-2028, budget LF 2026 » (feuille « Classif Prog-Admin-Eco »),
publiés par la DGB sur budgetbenin.bj.

Produit deux fichiers :
  - programmes : un programme (ou une dotation) par ligne, avec son ministère/institution,
    les années où il porte des crédits et les anomalies de référentiel détectées ;
  - credits : format long (programme × année × nature économique), en milliers de FCFA.
    La jointure avec les programmes se fait par `ligne_source` (numéro de ligne dans la feuille
    d'origine), seule clé unique : certains codes sont portés par deux programmes.

Les codes de programme sont normalisés sur 3 chiffres ; le code tel que publié est conservé
dans `code_source` et toute normalisation est signalée dans `anomalies`. Un même code utilisé
par deux programmes différents est signalé (CODE_REUTILISE) : le référentiel BIE doit donc
identifier un programme par (code, période de validité) et non par le code seul.

Usage :
    python extraire_programmes_budgetaires.py <classifications.xlsx> <programmes.csv> <credits.csv>
"""

from __future__ import annotations

import csv
import re
import sys
import unicodedata
from collections import defaultdict
from pathlib import Path

import openpyxl

FEUILLE = "Classif Prog-Admin-Eco"
LIGNE_ANNEES = 3  # index (0-based) des lignes d'en-tête dans la feuille
LIGNE_ENTETES = 5
PREMIERE_LIGNE_DONNEES = 6

# Libellés de colonnes -> nature économique canonique (titres de la nomenclature UEMOA)
NATURES = {
    "depenses de personnel": "T2_PERSONNEL",
    "depenses d'acquisitions de biens et services": "T3_BIENS_SERVICES",
    "depenses de transfert": "T4_TRANSFERTS",
    "ressources interieures": "T5_INVESTISSEMENT_RESSOURCES_INTERIEURES",
    "ressources exterieures": "T5_INVESTISSEMENT_RESSOURCES_EXTERIEURES",
    "ressources exterieures (dons)": "T5_INVESTISSEMENT_DONS",
    "ressources exterieures (prets)": "T5_INVESTISSEMENT_PRETS",
    "total des previsions": "TOTAL",
}


def normaliser(texte: object) -> str:
    brut = unicodedata.normalize("NFKD", str(texte)).encode("ascii", "ignore").decode()
    return re.sub(r"\s+", " ", brut).strip().lower()


def libelle_propre(texte: object) -> str:
    return re.sub(r"\s+", " ", str(texte)).strip()


def colonnes_par_annee(lignes: list[tuple]) -> dict[tuple[int, str], int]:
    annees, entetes = lignes[LIGNE_ANNEES], lignes[LIGNE_ENTETES]
    resultat: dict[tuple[int, str], int] = {}
    annee_courante = None
    for index, entete in enumerate(entetes):
        valeur_annee = annees[index] if index < len(annees) else None
        if valeur_annee not in (None, "") and str(valeur_annee).strip().isdigit():
            annee_courante = int(str(valeur_annee).strip())
        nature = NATURES.get(normaliser(entete)) if entete else None
        if annee_courante and nature:
            resultat[(annee_courante, nature)] = index
    return resultat


def sigle_depuis_pilotage(libelle: str) -> str | None:
    correspondance = re.search(r"services?\s+du\s+([A-Za-z]+)\s*$", libelle.strip(), re.IGNORECASE)
    return correspondance.group(1).upper() if correspondance else None


def extraire(chemin: Path) -> tuple[list[dict], list[dict]]:
    classeur = openpyxl.load_workbook(chemin, read_only=True, data_only=True)
    lignes = list(classeur[FEUILLE].iter_rows(values_only=True))
    colonnes = colonnes_par_annee(lignes)
    annees = sorted({annee for annee, _ in colonnes})

    programmes: list[dict] = []
    credits: list[dict] = []
    institution = None
    type_courant = "PROGRAMME"
    for numero_ligne, ligne in enumerate(lignes[PREMIERE_LIGNE_DONNEES:], start=PREMIERE_LIGNE_DONNEES + 1):
        code_brut, libelle = ligne[0], ligne[1]
        if code_brut in (None, "") and libelle in (None, ""):
            continue
        texte_code = libelle_propre(code_brut) if code_brut is not None else ""
        if libelle in (None, ""):
            entete = normaliser(texte_code)
            if entete.startswith("total"):
                continue
            if entete.startswith("dotations budgetaires"):
                type_courant, institution = "DOTATION", None
                continue
            institution = {"libelle": libelle_propre(texte_code), "sigle": None}
            continue

        anomalies = []
        code_source = texte_code
        chiffres = re.sub(r"\D", "", code_source)
        code = chiffres.lstrip("0").zfill(3) if chiffres else ""
        if code_source != code:
            anomalies.append("CODE_NORMALISE")
        libelle_programme = libelle_propre(libelle)

        if type_courant == "DOTATION":
            beneficiaire = re.sub(r"^dotations?\s+pour\s+(l['’]|la\s+|le\s+|les\s+)?", "", libelle_programme, flags=re.IGNORECASE)
            institution_programme = {"libelle": beneficiaire[:1].upper() + beneficiaire[1:], "sigle": None}
        else:
            institution_programme = institution or {"libelle": "", "sigle": None}
            sigle = sigle_depuis_pilotage(libelle_programme)
            if sigle and institution is not None and not institution["sigle"]:
                institution["sigle"] = sigle

        montants_par_annee = {}
        for annee in annees:
            total_index = colonnes.get((annee, "TOTAL"))
            total = ligne[total_index] if total_index is not None else None
            montants_par_annee[annee] = float(total) if isinstance(total, (int, float)) else 0.0
            for (annee_col, nature), index in colonnes.items():
                if annee_col != annee or nature == "TOTAL":
                    continue
                valeur = ligne[index]
                if isinstance(valeur, (int, float)) and valeur != 0:
                    credits.append(
                        {
                            "ligne_source": numero_ligne,
                            "programme_code": code,
                            "annee": annee,
                            "nature_economique": nature,
                            "montant_milliers_fcfa": round(float(valeur), 3),
                        }
                    )
        annees_avec_credits = [a for a, m in montants_par_annee.items() if m > 0]
        programmes.append(
            {
                "code": code,
                "code_source": code_source,
                "libelle": libelle_programme,
                "type": type_courant,
                "institution_libelle": institution_programme["libelle"],
                "institution": institution_programme,
                "premiere_annee_credits": min(annees_avec_credits) if annees_avec_credits else "",
                "derniere_annee_credits": max(annees_avec_credits) if annees_avec_credits else "",
                "credits_2026_milliers_fcfa": round(montants_par_annee.get(2026, 0.0), 3),
                "ligne_source": numero_ligne,
                "anomalies": anomalies,
            }
        )

    # un même code porté par plusieurs programmes distincts
    par_code = defaultdict(set)
    for p in programmes:
        par_code[p["code"]].add(normaliser(p["libelle"]))
    for p in programmes:
        if len(par_code[p["code"]]) > 1:
            p["anomalies"].append("CODE_REUTILISE")
    return programmes, credits


def ecrire(programmes: list[dict], credits: list[dict], sortie_programmes: Path, sortie_credits: Path) -> None:
    with open(sortie_programmes, "w", encoding="utf-8", newline="") as fichier:
        writer = csv.writer(fichier)
        writer.writerow(
            [
                "code", "code_source", "libelle", "type", "institution_libelle", "institution_sigle",
                "premiere_annee_credits", "derniere_annee_credits", "credits_2026_milliers_fcfa",
                "ligne_source", "anomalies",
            ]
        )
        for p in programmes:
            writer.writerow(
                [
                    p["code"], p["code_source"], p["libelle"], p["type"], p["institution"]["libelle"],
                    p["institution"]["sigle"] or "", p["premiere_annee_credits"], p["derniere_annee_credits"],
                    p["credits_2026_milliers_fcfa"], p["ligne_source"], "|".join(p["anomalies"]),
                ]
            )
    with open(sortie_credits, "w", encoding="utf-8", newline="") as fichier:
        champs = ["ligne_source", "programme_code", "annee", "nature_economique", "montant_milliers_fcfa"]
        writer = csv.DictWriter(fichier, fieldnames=champs)
        writer.writeheader()
        writer.writerows(credits)


def main() -> None:
    if len(sys.argv) != 4:
        print(__doc__)
        sys.exit(2)
    programmes, credits = extraire(Path(sys.argv[1]))
    ecrire(programmes, credits, Path(sys.argv[2]), Path(sys.argv[3]))
    avec_anomalies = [p for p in programmes if p["anomalies"]]
    print(f"{len(programmes)} programmes/dotations, {len(credits)} lignes de crédits, {len(avec_anomalies)} avec anomalies")
    for p in avec_anomalies:
        print(f"  {p['code_source']!r:>8} -> {p['code']} {p['libelle'][:60]} [{', '.join(p['anomalies'])}]")


if __name__ == "__main__":
    main()
