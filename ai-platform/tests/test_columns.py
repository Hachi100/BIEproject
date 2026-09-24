from decimal import Decimal

import pytest

from bie_ai.ptab_import.columns import combine_header_rows, map_columns, match_header


@pytest.mark.parametrize(
    ("header", "field"),
    [
        ("Activités", "activity_label"),
        ("Tâches à exécuter", "task_label"),
        ("Structure responsable", "responsible_unit"),
        ("Structures responsables de la mise en œuvre", "responsible_unit"),
        ("Coût total (FCFA)", "total_amount"),
        ("Montant prévisionnel (en milliers de FCFA)", "total_amount"),
        ("Sources de financement", "funding_source"),
        ("BN", "amount_national_budget"),
        ("FINEX", "amount_external"),
        ("P.U.", "unit_price"),
        ("Qté", "quantity"),
        ("Résultats attendus", "expected_result"),
        ("Indicateurs de performance", "indicator"),
        ("Imputation budgétaire", "economic_nature"),
        ("N°", "generic_code"),
        ("Désignation", "generic_label"),
        ("T3", "quarter_3"),
        ("Janv.", "month_1"),
        ("Mode d'exécution", "execution_mode"),
    ],
)
def test_intitules_courants(header, field):
    found = match_header(header)
    assert found is not None, header
    assert found[0] == field


def test_intitules_sans_rapport_ne_sont_pas_rapproches():
    assert match_header("Signature du DPAF") is None
    assert match_header("Mars 2026 à décembre") is None or match_header("Mars 2026 à décembre")[0] != "month_3"


def test_unite_monetaire_de_colonne():
    matches, _ = map_columns(["Activité", "Montant (en millions FCFA)", "BN"])
    by_field = {m.field: m for m in matches}
    assert by_field["total_amount"].multiplier == Decimal(1_000_000)
    assert by_field["amount_national_budget"].multiplier == Decimal(1)


def test_un_champ_n_est_attribue_qu_a_une_colonne():
    matches, unmapped = map_columns(["Activité", "Coût total", "Montant", "Observations"])
    assert [m.field for m in matches].count("total_amount") == 1
    assert len(unmapped) == 1


def test_en_tete_sur_deux_lignes():
    parent = ["Activités", "Chronogramme", "Chronogramme", "Financement", "Financement"]
    child = [None, "T1", "T2", "BN", "FINEX"]
    assert combine_header_rows(parent, child) == ["Activités", "T1", "T2", "BN", "FINEX"]
