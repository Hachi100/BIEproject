from fastapi.testclient import TestClient
from fixtures import ptab_ministere_classique

from bie_ai.main import app

client = TestClient(app)


def test_sante():
    assert client.get("/health").json() == {"status": "UP"}


def test_previsualisation_import():
    response = client.post(
        "/v1/ptab/import/preview",
        files={
            "file": (
                "ptab_ms_2026.xlsx",
                ptab_ministere_classique(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            )
        },
    )
    assert response.status_code == 200
    body = response.json()
    assert body["summary"]["activities"] == 2
    assert body["summary"]["tasks"] == 5
    assert body["activities"][0]["program_code"] == "045"


def test_fichier_non_ptab_refuse_avec_motif():
    response = client.post("/v1/ptab/import/preview", files={"file": ("notes.csv", b"a;b\n1;2\n", "text/csv")})
    assert response.status_code == 422
    assert "en-tête" in response.json()["detail"]


def test_format_refuse():
    response = client.post("/v1/ptab/import/preview", files={"file": ("ptab.pdf", b"%PDF", "application/pdf")})
    assert response.status_code == 415
