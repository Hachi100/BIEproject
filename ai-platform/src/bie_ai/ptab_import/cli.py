"""Analyse d'un ou plusieurs PTAB en ligne de commande.

    uv run bie-import-ptab chemin/vers/PTAB_2026.xlsx [autres fichiers…] [--json rapport.json]

Affiche, pour chaque fichier, la correspondance des colonnes, la structure reconnue, les
lignes mises en quarantaine et les écarts de totaux : de quoi vérifier rapidement comment
BIE lit un canevas de ministère avant de l'intégrer.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .models import RowOutcome
from .pipeline import PtabImportError, import_ptab


def _fcfa(value) -> str:
    return "—" if value is None else f"{value:,.0f}".replace(",", " ") + " FCFA"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Analyse de PTAB (xlsx, xls, csv) par le moteur d'import de BIE.")
    parser.add_argument("fichiers", nargs="+", type=Path)
    parser.add_argument("--feuille", help="Nom de la feuille à analyser")
    parser.add_argument("--exercice", type=int, help="Exercice budgétaire, si le fichier ne l'indique pas")
    parser.add_argument("--json", type=Path, help="Écrit le(s) rapport(s) complet(s) au format JSON")
    args = parser.parse_args(argv)

    reports = []
    status = 0
    for path in args.fichiers:
        print(f"\n=== {path.name}")
        try:
            report = import_ptab(path.read_bytes(), path.name, sheet_name=args.feuille, fiscal_year=args.exercice)
        except (PtabImportError, ValueError, OSError) as error:
            print(f"  ÉCHEC : {error}")
            status = 1
            continue
        reports.append(report)
        s = report.summary
        print(
            f"  Feuille « {report.sheet} », en-tête ligne(s) {report.header_rows}, exercice {report.fiscal_year or '?'}"
        )
        print(f"  Structure : {report.structure_mode}, unité monétaire × {report.default_multiplier}")
        print("  Colonnes reconnues :")
        for c in report.columns:
            print(f"    {c.column:>3}  {c.header[:45]:<45} → {c.field} ({c.method} {c.score:.0f})")
        if report.unmapped_columns:
            print(f"  Colonnes non reconnues : {', '.join(report.unmapped_columns)}")
        if report.missing_recommended_fields:
            print(f"  Informations absentes : {', '.join(report.missing_recommended_fields)}")
        print(f"  {s.activities} activités, {s.tasks} tâches, montant total {_fcfa(s.total_amount)}")
        print(
            f"  Lignes : {s.rows_total} au total, {s.imported} importées, {s.imported_with_warnings} avec avertissements, "
            f"{s.quarantined} en quarantaine, ignorées {s.ignored}"
        )
        for row in report.rows:
            if row.outcome in (RowOutcome.QUARANTINED, RowOutcome.IMPORTED_WITH_WARNINGS):
                print(f"    ligne {row.row} [{row.outcome.value}] {' / '.join(row.messages)}")
        for d in report.discrepancies:
            print(f"    écart ligne {d.row} [{d.kind}] {d.message}")
        for warning in report.warnings:
            print(f"    note : {warning}")

    if args.json:
        payload = [json.loads(r.model_dump_json()) for r in reports]
        args.json.write_text(
            json.dumps(payload if len(payload) != 1 else payload[0], ensure_ascii=False, indent=2), encoding="utf-8"
        )
        print(f"\nRapport complet écrit dans {args.json}")
    return status


if __name__ == "__main__":
    sys.exit(main())
