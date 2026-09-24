from datetime import date
from decimal import Decimal

import pytest

from bie_ai.ptab_import.values import parse_amount, parse_date, quarter_of


@pytest.mark.parametrize(
    ("raw", "expected"),
    [
        (1250000, Decimal("1250000")),
        (1250000.5, Decimal("1250000.5")),
        ("1 250 000", Decimal("1250000")),
        ("1 250 000 FCFA", Decimal("1250000")),
        ("1.250.000", Decimal("1250000")),
        ("1,250,000", Decimal("1250000")),
        ("3 250 000,50", Decimal("3250000.50")),
        ("12,5", Decimal("12.5")),
        ("1,5 M", Decimal("1500000")),
        ("2 Mds", Decimal("2000000000")),
        ("250 K FCFA", Decimal("250000")),
        ("", None),
        (None, None),
    ],
)
def test_montants_au_format_francais(raw, expected):
    parsed = parse_amount(raw)
    assert parsed.ok
    assert parsed.value == expected


@pytest.mark.parametrize("raw", ["#REF!", "#VALEUR!", "PM", "à déterminer", "environ 2 millions", "-5000"])
def test_montants_non_exploitables_sont_signales_sans_valeur_inventee(raw):
    parsed = parse_amount(raw)
    assert not parsed.ok
    assert parsed.value is None
    assert parsed.error


def test_dates():
    assert parse_date("15/03/2026").value == date(2026, 3, 15)
    assert parse_date("2026-03-15").value == date(2026, 3, 15)
    assert parse_date(46096).value == date(2026, 3, 15)  # numéro de série Excel
    assert parse_date("Mars", fiscal_year=2026).value == date(2026, 3, 1)
    assert not parse_date("31/02/2026").ok


@pytest.mark.parametrize(
    ("header", "quarter"),
    [("T1", 1), ("Trim 2", 2), ("3e trimestre", 3), ("4ème trimestre", 4), ("Q4", 4), ("Total", None)],
)
def test_colonnes_de_trimestre(header, quarter):
    assert quarter_of(header) == quarter
