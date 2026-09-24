"""Extraction de l'e-Répertoire des prix de référence (PDF) vers un CSV structuré.

Source : « e-Répertoire des prix de référence », Direction Nationale du Contrôle
Financier (DNCF), publié sur https://erepertoire.finances.bj/documents
(format de la 19e édition, versions 26.x).

Chaque article du PDF comporte : un code (arrimé à la Nomenclature Budgétaire de l'État,
au Plan Comptable de l'État et au Plan des Comptes Matières), une famille, une
désignation, des spécifications techniques, une unité de mesure, une borne inférieure (BI)
et une borne supérieure (BS). Les prix sont TTC pour les produits assujettis à la TVA.

Le tableau est découpé en lignes à l'aide des bordures de cellules (rectangles du PDF),
car le texte des cellules est centré verticalement et s'étale sur plusieurs lignes.
Une ligne sans code en haut de page est la suite de l'article de la page précédente.

Aucune correction silencieuse : les incohérences sont signalées dans `anomalies`.

Usage :
    python extraire_repertoire_prix.py <repertoire.pdf> <sortie.csv[.gz]> [--edition "19e édition v26.3"] [--date 2026-06]
"""

from __future__ import annotations

import argparse
import csv
import gzip
import re
from collections import Counter
from dataclasses import dataclass, field
from pathlib import Path

import pdfplumber

COLONNES = ["code", "famille", "designation", "specifications", "unite", "bi", "bs"]
MOTIF_CODE = re.compile(r"^\d{4} \d{4} \d{3} \d{4}$")


@dataclass
class Article:
    page: int
    cellules: dict[str, list[str]] = field(default_factory=lambda: {c: [] for c in COLONNES})

    def texte(self, colonne: str) -> str:
        return re.sub(r"\s+", " ", " ".join(self.cellules[colonne])).strip()

    def montant(self, colonne: str) -> int | None:
        brut = re.sub(r"[^\d]", "", self.texte(colonne))
        return int(brut) if brut else None


def separateurs_verticaux(page) -> list[float]:
    """Abscisses des bordures verticales du tableau (une par frontière de colonne)."""
    xs = sorted({round(r["x0"]) for r in page.rects if r["width"] < 1 and r["height"] > 5})
    return [float(x) for x in xs]


def separateurs_horizontaux(page, x_min: float) -> list[float]:
    """Ordonnées des bordures horizontales de la première colonne (une par ligne d'article)."""
    ys = sorted({round(r["top"], 1) for r in page.rects if r["height"] < 1 and r["width"] > 50 and r["x0"] < x_min + 20})
    return ys


def cellules_de_la_bande(mots: list[dict], xs: list[float]) -> dict[str, list[str]]:
    cellules: dict[str, list[str]] = {c: [] for c in COLONNES}
    for mot in sorted(mots, key=lambda m: (round(m["top"]), m["x0"])):
        index = sum(1 for x in xs if mot["x0"] >= x) - 1
        if 0 <= index < len(COLONNES):
            cellules[COLONNES[index]].append(mot["text"])
    return cellules


def extraire(chemin_pdf: Path) -> list[Article]:
    articles: list[Article] = []
    with pdfplumber.open(chemin_pdf) as pdf:
        for numero_page, page in enumerate(pdf.pages, start=1):
            xs = separateurs_verticaux(page)
            if len(xs) != len(COLONNES) + 1:
                continue  # page sans tableau d'articles (préface, guides, schémas)
            ys = separateurs_horizontaux(page, xs[0])
            mots = page.extract_words()
            for haut, bas in zip(ys, ys[1:]):
                bande = [m for m in mots if haut <= (m["top"] + m["bottom"]) / 2 < bas]
                if not bande:
                    continue
                cellules = cellules_de_la_bande(bande, xs)
                code = " ".join(cellules["code"])
                if code == "CODE":
                    continue  # ligne d'en-tête
                if MOTIF_CODE.match(code) or (code and not articles):
                    article = Article(page=numero_page)
                    article.cellules = cellules
                    articles.append(article)
                elif articles:
                    # suite d'un article commencé sur la page précédente
                    for colonne, valeurs in cellules.items():
                        articles[-1].cellules[colonne].extend(valeurs)
    return articles


def anomalies(article: Article, codes_dupliques: set[str]) -> list[str]:
    constats = []
    code = article.texte("code")
    if not MOTIF_CODE.match(code):
        constats.append("CODE_INVALIDE")
    elif code in codes_dupliques:
        constats.append("CODE_DUPLIQUE")
    if not article.texte("designation"):
        constats.append("DESIGNATION_MANQUANTE")
    if not article.texte("unite"):
        constats.append("UNITE_MANQUANTE")
    bi, bs = article.montant("bi"), article.montant("bs")
    for borne, valeur in (("BI", bi), ("BS", bs)):
        if "#" in article.texte(borne.lower()):
            constats.append(f"{borne}_ILLISIBLE_DANS_LA_SOURCE")  # cellule Excel « ####### » dans le PDF publié
        elif valeur is None:
            constats.append(f"{borne}_MANQUANTE")
    if bi is not None and bs is not None and bi > bs:
        constats.append("BI_SUPERIEURE_A_BS")
    return constats


def ecrire_csv(articles: list[Article], sortie: Path, edition: str, date_edition: str) -> Counter:
    compte_codes = Counter(a.texte("code") for a in articles)
    dupliques = {code for code, n in compte_codes.items() if n > 1}
    statistiques: Counter = Counter()
    ouvrir = gzip.open if sortie.suffix == ".gz" else open
    with ouvrir(sortie, "wt", encoding="utf-8", newline="") as fichier:
        writer = csv.writer(fichier)
        writer.writerow(
            [
                "code", "famille", "designation", "specifications", "unite",
                "borne_inferieure", "borne_superieure", "edition", "date_edition", "page_source", "anomalies",
            ]
        )
        for a in articles:
            constats = anomalies(a, dupliques)
            statistiques.update(constats)
            writer.writerow(
                [
                    a.texte("code"), a.texte("famille"), a.texte("designation"), a.texte("specifications"),
                    a.texte("unite"), a.montant("bi") or "", a.montant("bs") or "",
                    edition, date_edition, a.page, "|".join(constats),
                ]
            )
    return statistiques


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("pdf", type=Path)
    parser.add_argument("sortie", type=Path)
    parser.add_argument("--edition", default="19e édition v26.3")
    parser.add_argument("--date", default="2026-06")
    args = parser.parse_args()

    articles = extraire(args.pdf)
    statistiques = ecrire_csv(articles, args.sortie, args.edition, args.date)
    print(f"{len(articles)} articles extraits vers {args.sortie}")
    for anomalie, nombre in statistiques.most_common():
        print(f"  {anomalie}: {nombre}")


if __name__ == "__main__":
    main()
