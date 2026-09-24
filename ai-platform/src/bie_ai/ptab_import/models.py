"""Modèle de sortie de l'import : données canoniques BIE + traçabilité ligne à ligne."""

from __future__ import annotations

from datetime import date
from decimal import Decimal
from enum import StrEnum

from pydantic import BaseModel, Field


class RowOutcome(StrEnum):
    IMPORTED = "IMPORTED"
    IMPORTED_WITH_WARNINGS = "IMPORTED_WITH_WARNINGS"
    IGNORED_EMPTY = "IGNORED_EMPTY"
    IGNORED_TITLE = "IGNORED_TITLE"
    IGNORED_HEADER = "IGNORED_HEADER"
    IGNORED_TOTAL = "IGNORED_TOTAL"
    QUARANTINED = "QUARANTINED"


class Level(StrEnum):
    PROGRAM = "PROGRAM"
    ACTION = "ACTION"
    ACTIVITY = "ACTIVITY"
    TASK = "TASK"


class ColumnMapping(BaseModel):
    column: str = Field(description="Lettre de la colonne dans le fichier (A, B, …)")
    header: str
    field: str
    score: float
    method: str
    multiplier: Decimal = Decimal(1)


class Period(BaseModel):
    start: date | None = None
    end: date | None = None
    quarters: list[int] = Field(default_factory=list)
    months: list[int] = Field(default_factory=list)
    quarterly_amounts: dict[int, Decimal] = Field(default_factory=dict)
    text: str | None = None


class Funding(BaseModel):
    national_budget: Decimal | None = None
    external: Decimal | None = None
    grants: Decimal | None = None
    loans: Decimal | None = None
    counterpart: Decimal | None = None
    source_text: str | None = None
    source: str | None = Field(default=None, description="NATIONAL_BUDGET, EXTERNAL, GRANT, LOAN, COUNTERPART, MIXED")


class ImportedTask(BaseModel):
    source_row: int
    code: str | None = None
    label: str
    responsible: str | None = None
    period: Period = Field(default_factory=Period)
    quantity: Decimal | None = None
    unit: str | None = None
    unit_price: Decimal | None = None
    amount: Decimal | None = None
    funding: Funding = Field(default_factory=Funding)
    economic_nature: str | None = None
    observations: str | None = None


class ImportedActivity(BaseModel):
    source_row: int
    program_code: str | None = None
    program_label: str | None = None
    action_code: str | None = None
    action_label: str | None = None
    code: str | None = None
    label: str
    responsible_unit: str | None = None
    associated_units: str | None = None
    expected_result: str | None = None
    indicator: str | None = None
    target: str | None = None
    location: str | None = None
    execution_mode: str | None = None
    period: Period = Field(default_factory=Period)
    declared_amount: Decimal | None = Field(default=None, description="Montant saisi sur la ligne de l'activité")
    tasks_amount: Decimal | None = Field(default=None, description="Somme des montants des tâches")
    amount: Decimal | None = Field(
        default=None, description="Montant retenu : détail des tâches, sinon montant déclaré"
    )
    funding: Funding = Field(default_factory=Funding)
    economic_nature: str | None = None
    observations: str | None = None
    tasks: list[ImportedTask] = Field(default_factory=list)


class RowReport(BaseModel):
    row: int = Field(description="Numéro de ligne dans le fichier (1 = première ligne)")
    outcome: RowOutcome
    level: Level | None = None
    messages: list[str] = Field(default_factory=list)


class Discrepancy(BaseModel):
    row: int
    kind: str
    message: str
    declared: Decimal | None = None
    computed: Decimal | None = None


class SheetCandidate(BaseModel):
    name: str
    header_row: int | None
    mapped_columns: int


class Summary(BaseModel):
    rows_total: int
    imported: int
    imported_with_warnings: int
    ignored: dict[str, int]
    quarantined: int
    activities: int
    tasks: int
    total_amount: Decimal


class ImportReport(BaseModel):
    file_name: str
    sheet: str
    sheets: list[SheetCandidate]
    header_rows: list[int]
    fiscal_year: int | None
    default_multiplier: Decimal
    structure_mode: str
    columns: list[ColumnMapping]
    unmapped_columns: list[str]
    missing_recommended_fields: list[str]
    activities: list[ImportedActivity]
    rows: list[RowReport]
    discrepancies: list[Discrepancy]
    warnings: list[str]
    summary: Summary
