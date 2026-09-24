"""Lecture des valeurs de cellules telles qu'elles apparaissent dans les PTAB des ministères.

Les fichiers mélangent nombres Excel, textes formatés à la française (« 1 250 000 »,
« 1.250.000 FCFA », « 12,5 »), erreurs Excel (« #REF! ») et mentions libres
(« PM », « à déterminer »). Chaque fonction retourne soit une valeur, soit une raison
d'échec explicite : aucune valeur n'est inventée.
"""

from __future__ import annotations

import re
import unicodedata
from dataclasses import dataclass
from datetime import date, datetime, timedelta
from decimal import Decimal, InvalidOperation

EXCEL_ERRORS = {"#REF!", "#VALUE!", "#DIV/0!", "#N/A", "#NAME?", "#NUM!", "#NULL!", "#VALEUR!", "#NOM?", "#N/D"}
EMPTY_MARKERS = {"", "-", "--", "—", "néant", "neant", "ras", "0"}
TO_BE_DEFINED = {
    "pm",
    "p.m.",
    "p.m",
    "pour memoire",
    "pour mémoire",
    "a determiner",
    "à déterminer",
    "ad",
    "nd",
    "n.d.",
}

MONTHS = {
    "janv": 1,
    "janvier": 1,
    "jan": 1,
    "fevr": 2,
    "fev": 2,
    "fevrier": 2,
    "feb": 2,
    "mars": 3,
    "mar": 3,
    "avr": 4,
    "avril": 4,
    "apr": 4,
    "mai": 5,
    "may": 5,
    "juin": 6,
    "jun": 6,
    "juil": 7,
    "juillet": 7,
    "jul": 7,
    "aout": 8,
    "aou": 8,
    "aug": 8,
    "sept": 9,
    "sep": 9,
    "septembre": 9,
    "oct": 10,
    "octobre": 10,
    "nov": 11,
    "novembre": 11,
    "dec": 12,
    "decembre": 12,
}


def normalize(text: object) -> str:
    """Minuscules, sans accents, ponctuation réduite à des espaces."""
    raw = unicodedata.normalize("NFKD", str(text)).encode("ascii", "ignore").decode().lower()
    raw = re.sub(r"[^a-z0-9%]+", " ", raw)
    return re.sub(r"\s+", " ", raw).strip()


@dataclass(frozen=True)
class Parsed:
    value: object | None
    error: str | None = None

    @property
    def ok(self) -> bool:
        return self.error is None


def is_blank(value: object) -> bool:
    return value is None or (isinstance(value, str) and value.strip() == "")


def parse_amount(value: object) -> Parsed:
    """Montant en unités de la colonne (le multiplicateur milliers/millions est appliqué ailleurs)."""
    if is_blank(value):
        return Parsed(None)
    if isinstance(value, bool):
        return Parsed(None, f"valeur booléenne inattendue : {value}")
    if isinstance(value, (int, float, Decimal)):
        amount = Decimal(str(value))
        return Parsed(amount) if amount >= 0 else Parsed(None, f"montant négatif : {value}")
    text = str(value).strip()
    if text.upper() in EXCEL_ERRORS:
        return Parsed(None, f"erreur Excel dans la cellule : {text}")
    lowered = normalize(text)
    if lowered in TO_BE_DEFINED:
        return Parsed(None, f"montant non chiffré : « {text} »")
    multiplier = Decimal(1)
    suffix = re.search(r"\b(mds?|milliards?|m|millions?|k|milliers?)\s*(f\s*cfa|fcfa|xof)?\s*$", lowered)
    if suffix:
        unit = suffix.group(1)
        if unit.startswith(("md", "milliard")):
            multiplier = Decimal(1_000_000_000)
        elif unit.startswith("m") and not unit.startswith("millier"):
            multiplier = Decimal(1_000_000)
        else:
            multiplier = Decimal(1_000)
        text = re.sub(r"(?i)\s*(mds?|milliards?|millions?|milliers?|m|k)\s*(f\s*cfa|fcfa|xof)?\s*$", "", text)
    text = re.sub(r"(?i)\s*(f\s*cfa|fcfa|xof|francs?)\s*$", "", text)
    compact = re.sub(r"[\s  ]", "", text)
    if not re.fullmatch(r"-?[\d.,']+", compact):
        return Parsed(None, f"montant illisible : « {value} »")
    compact = compact.replace("'", "")
    if "," in compact and "." in compact:
        # le dernier séparateur est le séparateur décimal
        decimal_sep = "," if compact.rfind(",") > compact.rfind(".") else "."
        thousands_sep = "." if decimal_sep == "," else ","
        compact = compact.replace(thousands_sep, "").replace(decimal_sep, ".")
    elif "," in compact:
        parts = compact.split(",")
        compact = compact.replace(",", "") if len(parts) > 2 or len(parts[-1]) == 3 else compact.replace(",", ".")
    elif "." in compact:
        parts = compact.split(".")
        if len(parts) > 2 or len(parts[-1]) == 3:
            compact = compact.replace(".", "")
    try:
        amount = Decimal(compact) * multiplier
    except InvalidOperation:
        return Parsed(None, f"montant illisible : « {value} »")
    if amount < 0:
        return Parsed(None, f"montant négatif : {value}")
    return Parsed(amount)


def parse_date(value: object, fiscal_year: int | None = None) -> Parsed:
    if is_blank(value):
        return Parsed(None)
    if isinstance(value, datetime):
        return Parsed(value.date())
    if isinstance(value, date):
        return Parsed(value)
    if isinstance(value, (int, float)) and 20000 < float(value) < 80000:
        # numéro de série Excel
        return Parsed(date(1899, 12, 30) + timedelta(days=int(value)))
    text = str(value).strip()
    match = re.fullmatch(r"(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2,4})", text)
    if match:
        day, month, year = (int(g) for g in match.groups())
        year = year + 2000 if year < 100 else year
        try:
            return Parsed(date(year, month, day))
        except ValueError:
            return Parsed(None, f"date invalide : « {text} »")
    match = re.fullmatch(r"(\d{4})-(\d{2})-(\d{2})", text)
    if match:
        try:
            return Parsed(date(*(int(g) for g in match.groups())))
        except ValueError:
            return Parsed(None, f"date invalide : « {text} »")
    month = month_of(text)
    if month and fiscal_year:
        return Parsed(date(fiscal_year, month, 1))
    return Parsed(None, f"date illisible : « {text} »")


def month_of(text: object) -> int | None:
    words = normalize(text).split()
    if not words:
        return None
    return MONTHS.get(words[0]) or MONTHS.get(words[0][:4]) or MONTHS.get(words[0][:3])


def quarter_of(header: object) -> int | None:
    """T1, Trim 2, 3e trimestre, Q4…"""
    text = normalize(header)
    match = re.fullmatch(r"(?:t|q|trim|trimestre)\s*([1-4])", text) or re.fullmatch(
        r"([1-4])\s*(?:er|e|eme|ieme|nd|me)?\s*trim(?:estre)?", text
    )
    return int(match.group(1)) if match else None


def is_schedule_mark(value: object) -> bool:
    """Une cellule de chronogramme cochée : « X », « x », « ✓ », « 1 », un montant, une couleur non lisible exclue."""
    if is_blank(value):
        return False
    if isinstance(value, (int, float, Decimal)):
        return value != 0
    return normalize(value) not in {"", "0", "non", "n"}


def clean_text(value: object) -> str | None:
    if is_blank(value):
        return None
    text = re.sub(r"\s+", " ", str(value)).strip()
    return text or None
