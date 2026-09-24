"""API de la plateforme IA de BIE (incrément 1 : import intelligent des PTAB)."""

from __future__ import annotations

from typing import Annotated

from fastapi import FastAPI, File, Form, HTTPException, UploadFile

from .ptab_import.models import ImportReport
from .ptab_import.pipeline import PtabImportError, import_ptab

MAX_UPLOAD_BYTES = 25 * 1024 * 1024

app = FastAPI(
    title="BIE — Plateforme IA",
    version="0.1.0",
    description="Services d'intelligence de Budget Intelligence Engine. Les sorties sont des propositions "
    "structurées : elles sont validées par l'utilisateur puis par les règles du cœur métier.",
)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/v1/ptab/import/preview", response_model=ImportReport)
async def preview_ptab_import(
    file: Annotated[UploadFile, File(description="PTAB au format .xlsx, .xls ou .csv")],
    sheet: Annotated[str | None, Form(description="Feuille à importer (par défaut : la plus proche d'un PTAB)")] = None,
    fiscal_year: Annotated[int | None, Form(description="Exercice, si le fichier ne l'indique pas")] = None,
) -> ImportReport:
    """Analyse un PTAB et retourne sa structure canonique, sans rien enregistrer.

    Chaque ligne du fichier est rattachée à un résultat (importée, ignorée ou en quarantaine)
    avec son motif, et les totaux déclarés sont rapprochés des montants détaillés.
    """
    content = await file.read(MAX_UPLOAD_BYTES + 1)
    if len(content) > MAX_UPLOAD_BYTES:
        raise HTTPException(status_code=413, detail="Fichier trop volumineux (25 Mo maximum).")
    if not content:
        raise HTTPException(status_code=400, detail="Fichier vide.")
    try:
        return import_ptab(content, file.filename or "ptab.xlsx", sheet_name=sheet, fiscal_year=fiscal_year)
    except PtabImportError as error:
        raise HTTPException(status_code=422, detail=str(error)) from error
    except ValueError as error:
        raise HTTPException(status_code=415, detail=str(error)) from error
