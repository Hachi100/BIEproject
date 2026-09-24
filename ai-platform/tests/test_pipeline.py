from decimal import Decimal

import pytest
from fixtures import ptab_codes_hierarchiques, ptab_csv_francais, ptab_ministere_classique

from bie_ai.ptab_import.models import RowOutcome
from bie_ai.ptab_import.pipeline import PtabImportError, import_ptab


def outcomes(report):
    return {row.row: row.outcome for row in report.rows}


def assert_zero_silent_loss(report):
    """Chaque ligne du fichier a exactement un résultat."""
    numbers = [row.row for row in report.rows]
    assert numbers == list(range(1, report.summary.rows_total + 1))
    counted = (
        report.summary.imported
        + report.summary.imported_with_warnings
        + report.summary.quarantined
        + sum(report.summary.ignored.values())
    )
    assert counted == report.summary.rows_total


@pytest.fixture(scope="module")
def report_classique():
    return import_ptab(ptab_ministere_classique(), "ptab_ms_2026.xlsx")


@pytest.fixture(scope="module")
def report_codes():
    return import_ptab(ptab_codes_hierarchiques(), "ptab_mef.xlsx")


@pytest.fixture(scope="module")
def report_csv():
    return import_ptab(ptab_csv_francais(), "export.csv", fiscal_year=2026)


class TestPtabMinistereClassique:
    @pytest.fixture
    def report(self, report_classique):
        return report_classique

    def test_entete_et_colonnes(self, report):
        assert report.header_rows == [4, 5]
        assert report.fiscal_year == 2026
        assert report.structure_mode == "EXPLICIT"
        fields = {c.field for c in report.columns}
        assert {
            "program_label",
            "action_label",
            "activity_label",
            "task_label",
            "responsible_unit",
            "quarter_1",
            "quarter_4",
            "total_amount",
            "funding_source",
        } <= fields
        assert report.missing_recommended_fields == []

    def test_hierarchie_reconstituee(self, report):
        assert [a.label for a in report.activities] == [
            "Organiser la revue à mi-parcours du PTA",
            "Former les gestionnaires de crédits au SIGFP",
        ]
        revue, formation = report.activities
        assert revue.program_code == "045"
        assert revue.program_label == "Pilotage et soutien aux services du MS"
        assert revue.action_label == "Gestion des ressources humaines"
        assert len(revue.tasks) == 3
        assert revue.expected_result == "Le taux d'exécution à mi-parcours est connu"
        assert revue.responsible_unit == "DPAF"
        assert revue.amount == Decimal(5_000_000)
        assert revue.tasks[1].period.quarters == [3]
        assert revue.tasks[0].funding.source == "NATIONAL_BUDGET"
        assert formation.tasks[1].funding.source == "EXTERNAL"

    def test_erreur_excel_signalee_sans_perte(self, report):
        formation = report.activities[1]
        assert formation.tasks[0].amount is None
        row = next(r for r in report.rows if r.row == 9)
        assert row.outcome == RowOutcome.IMPORTED_WITH_WARNINGS
        assert any("#REF!" in m for m in row.messages)

    def test_totaux_rapproches(self, report):
        # sous-total déclaré 7 000 000 ; détail lisible = 5 000 000 + 2 000 000 (la cellule #REF! est illisible)
        assert all(d.kind != "SOUS_TOTAL" for d in report.discrepancies)
        assert report.summary.total_amount == Decimal(7_000_000)

    def test_ligne_orpheline_en_quarantaine(self, report):
        assert outcomes(report)[13] == RowOutcome.QUARANTINED

    def test_zero_perte_silencieuse(self, report):
        assert_zero_silent_loss(report)
        assert outcomes(report)[1] == RowOutcome.IGNORED_TITLE
        assert outcomes(report)[11] == RowOutcome.IGNORED_TOTAL
        assert outcomes(report)[14] == RowOutcome.IGNORED_TOTAL


class TestPtabCodesHierarchiques:
    @pytest.fixture
    def report(self, report_codes):
        return report_codes

    def test_unite_en_milliers(self, report):
        assert report.default_multiplier == Decimal(1000)
        assert report.structure_mode == "CODED"

    def test_niveaux_deduits_des_codes(self, report):
        assert [a.code for a in report.activities] == ["026.1.01", "026.1.02"]
        conferences, archivage = report.activities
        assert conferences.program_code == "026"
        assert conferences.action_label == "Coordination administrative"
        assert [t.code for t in conferences.tasks] == ["026.1.01.1", "026.1.01.2"]
        assert any("profondeur des codes" in w for w in report.warnings)

    def test_montants_calcules_et_rapproches(self, report):
        conferences, archivage = report.activities
        # quantité × prix unitaire, en milliers
        assert conferences.tasks[0].amount == Decimal(250_000)
        assert conferences.amount == Decimal(1_250_000)
        assert archivage.funding.national_budget == Decimal(3_000_000)
        assert archivage.funding.external == Decimal(6_000_000)
        assert archivage.funding.source == "MIXED"
        # financement saisi sur la ligne de l'activité : pas de double comptage avec les tâches
        assert conferences.funding.national_budget == Decimal(1_250_000)
        # le montant du programme (10 250) est rapproché de la somme de ses activités
        assert report.summary.total_amount == Decimal(10_250_000)
        assert report.discrepancies == []

    def test_ecart_programme_activites_detecte(self):
        report = import_ptab(ptab_codes_hierarchiques(montant_programme=12000), "ptab_mef.xlsx")
        ecart = [d for d in report.discrepancies if d.kind == "PROGRAM_VS_ACTIVITES"]
        assert len(ecart) == 1
        assert ecart[0].declared == Decimal(12_000_000)
        assert ecart[0].computed == Decimal(10_250_000)

    def test_dates_incoherentes_signalees(self, report):
        archivage = report.activities[1]
        assert archivage.period.start is None
        row = next(r for r in report.rows if r.row == 9)
        assert any("antérieure" in m for m in row.messages)

    def test_zero_perte_silencieuse(self, report):
        assert_zero_silent_loss(report)


class TestPtabCsv:
    @pytest.fixture
    def report(self, report_csv):
        return report_csv

    def test_lecture(self, report):
        assert report.structure_mode == "EXPLICIT"
        vaccination, supervision = report.activities
        assert vaccination.code == "A1"
        assert [t.label for t in vaccination.tasks] == ["Acheter les vaccins", "Mobiliser les relais communautaires"]
        assert vaccination.tasks[0].period.quarters == [1, 2]
        assert vaccination.tasks[0].amount == Decimal(12_500_000)
        assert vaccination.tasks[1].amount == Decimal(1_500_000)
        assert vaccination.execution_mode == "INDIRECT"
        assert supervision.declared_amount == Decimal("3250000.50")
        assert supervision.period.months == [3, 4, 5, 6]
        assert supervision.funding.source == "EXTERNAL"
        # activité sans financement saisi : agrégé depuis ses tâches
        assert vaccination.funding.source == "NATIONAL_BUDGET"
        assert vaccination.period.quarters == [1, 2]

    def test_montant_sans_libelle_en_quarantaine_et_total_rapproche(self, report):
        assert outcomes(report)[5] == RowOutcome.QUARANTINED
        assert [d.kind for d in report.discrepancies] == ["TOTAL_GENERAL"]
        total = [d for d in report.discrepancies if d.kind == "TOTAL_GENERAL"]
        assert total and total[0].declared == Decimal("17600000.50")
        assert total[0].computed == Decimal("17250000.50")

    def test_zero_perte_silencieuse(self, report):
        assert_zero_silent_loss(report)


def test_fichier_sans_structure_de_ptab():
    import io

    import openpyxl

    wb = openpyxl.Workbook()
    wb.active.append(["Nom", "Prénom", "Téléphone"])
    wb.active.append(["DOSSOU", "Ange", "0197000000"])
    buffer = io.BytesIO()
    wb.save(buffer)
    with pytest.raises(PtabImportError, match="en-tête"):
        import_ptab(buffer.getvalue(), "contacts.xlsx")


def test_format_non_supporte():
    with pytest.raises(ValueError, match="Format non pris en charge"):
        import_ptab(b"%PDF-1.4", "ptab.pdf")
