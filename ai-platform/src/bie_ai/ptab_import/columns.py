"""Reconnaissance des colonnes d'un PTAB : chaque ministère a son canevas, les intitulés varient.

Le rapprochement se fait en trois temps :
  1. correspondance exacte avec un synonyme connu (score 100) ;
  2. correspondance approchée (rapidfuzz), au-dessus d'un seuil ;
  3. colonnes de chronogramme (T1..T4, mois) reconnues par motif.
Le dictionnaire de synonymes est volontairement explicite : il s'enrichit à partir des
PTAB réels des ministères (voir synonymes.json).
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from decimal import Decimal
from functools import lru_cache
from pathlib import Path

from rapidfuzz import fuzz

from .values import MONTHS, is_blank, month_of, normalize, quarter_of

FUZZY_THRESHOLD = 86

AMOUNT_FIELDS = {
    "total_amount",
    "unit_price",
    "amount_national_budget",
    "amount_external",
    "amount_grants",
    "amount_loans",
    "amount_counterpart",
}
NUMBER_FIELDS = AMOUNT_FIELDS | {"quantity"}
MULTI_COLUMN_PREFIXES = ("quarter_", "month_")


@dataclass(frozen=True)
class ColumnMatch:
    column: int  # index 0-based
    header: str
    field: str
    score: float
    method: str  # EXACT, FUZZY, PATTERN
    multiplier: Decimal = Decimal(1)


@lru_cache(maxsize=1)
def synonyms() -> dict[str, list[str]]:
    path = Path(__file__).with_name("synonymes.json")
    raw = json.loads(path.read_text(encoding="utf-8"))
    return {field: [normalize(s) for s in values] for field, values in raw["champs"].items()}


def unit_multiplier(text: object) -> Decimal | None:
    """Unité monétaire déclarée dans un intitulé : « (en milliers de FCFA) », « Montant (M FCFA) »."""
    t = normalize(text)
    if re.search(r"\bmilliards?\b|\bmds?\s*f?\s*cfa\b", t):
        return Decimal(1_000_000_000)
    if re.search(r"\bmillions?\b|\bm\s*f?\s*cfa\b|\bmfcfa\b", t):
        return Decimal(1_000_000)
    if re.search(r"\bmilliers?\b|\bk\s*f?\s*cfa\b|\bkfcfa\b|\b000\s*f?\s*cfa\b", t):
        return Decimal(1_000)
    return None


def strip_units(header: str) -> str:
    """Retire les précisions entre parenthèses et les unités, qui gênent le rapprochement."""
    text = re.sub(r"\([^)]*\)", " ", header)
    text = normalize(text)
    text = re.sub(r"\b(en )?(milliers?|millions?|milliards?)( de)?( f ?cfa| fcfa| xof)?\b", " ", text)
    text = re.sub(r"\b(f ?cfa|xof|francs? cfa)\b", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def match_header(header: object) -> tuple[str, float, str] | None:
    """Retourne (champ, score, méthode) pour un intitulé de colonne, ou None."""
    if is_blank(header):
        return None
    raw = str(header)
    quarter = quarter_of(raw)
    if quarter:
        return f"quarter_{quarter}", 100.0, "PATTERN"
    month_header = re.fullmatch(r"([a-z]+)( \d{2,4})?", normalize(raw))
    if month_header and month_header.group(1) in MONTHS:
        return f"month_{month_of(raw)}", 100.0, "PATTERN"
    text = strip_units(raw)
    if not text:
        return None
    best: tuple[str, float, str] | None = None
    for field, values in synonyms().items():
        if text in values:
            return field, 100.0, "EXACT"
        for synonym in values:
            if len(synonym) <= 3 or len(text) <= 3:
                continue  # les sigles courts (BN, PU…) ne sont acceptés qu'en correspondance exacte
            score = fuzz.token_sort_ratio(text, synonym)
            if len(synonym.split()) >= 2:
                score = max(score, fuzz.token_set_ratio(text, synonym) - 4)
            if score >= FUZZY_THRESHOLD and (best is None or score > best[1]):
                best = (field, float(score), "FUZZY")
    return best


def map_columns(
    headers: list[object], default_multiplier: Decimal | None = None
) -> tuple[list[ColumnMatch], list[int]]:
    """Associe chaque colonne à au plus un champ ; chaque champ (hors chronogramme) à une seule colonne."""
    candidates: list[ColumnMatch] = []
    for index, header in enumerate(headers):
        found = match_header(header)
        if found:
            field, score, method = found
            multiplier = Decimal(1)
            if field in AMOUNT_FIELDS:
                multiplier = unit_multiplier(header) or default_multiplier or Decimal(1)
            candidates.append(ColumnMatch(index, str(header).strip(), field, score, method, multiplier))
    chosen: dict[str, ColumnMatch] = {}
    extra: list[ColumnMatch] = []
    for candidate in sorted(candidates, key=lambda c: (-c.score, c.column)):
        if candidate.field.startswith(MULTI_COLUMN_PREFIXES):
            extra.append(candidate)
        elif candidate.field not in chosen:
            chosen[candidate.field] = candidate
    matches = sorted(list(chosen.values()) + extra, key=lambda c: c.column)
    mapped = {m.column for m in matches}
    unmapped = [i for i, h in enumerate(headers) if not is_blank(h) and i not in mapped]
    return matches, unmapped


def combine_header_rows(parent: list[object], child: list[object]) -> list[object]:
    """Intitulés sur deux lignes (« Chronogramme » / T1..T4, « Financement » / BN, FINEX)."""
    combined: list[object] = []
    for index in range(max(len(parent), len(child))):
        top = parent[index] if index < len(parent) else None
        bottom = child[index] if index < len(child) else None
        if is_blank(bottom):
            combined.append(top)
        elif match_header(bottom) or is_blank(top):
            combined.append(bottom)
        elif match_header(f"{top} {bottom}"):
            combined.append(f"{top} {bottom}")
        else:
            combined.append(top)
    return combined
