"""Télécharge les documents officiels référencés dans docs/sources/catalogue.csv.

    python scripts/sources/telecharger_sources.py [--dossier data/sources] [--id lolf --id pap_2026]
                                                  [--ca-bundle chemin/bundle.pem]

Les documents ne sont pas versionnés dans Git (volumineux, republiés par les administrations) :
le catalogue fait foi et ce script reconstitue le corpus localement (data/ est ignoré par Git).

Note : le serveur erepertoire.finances.bj ne transmet pas le certificat intermédiaire
« Go Daddy Secure Certificate Authority - G2 ». Si la vérification TLS échoue, compléter
la chaîne sans la désactiver : récupérer http://certificates.godaddy.com/repository/gdig2.crt,
le convertir en PEM et le concaténer au magasin de certificats passé avec --ca-bundle.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import re
import ssl
import sys
import time
import urllib.request
from pathlib import Path

RACINE = Path(__file__).resolve().parents[2]
CATALOGUE = RACINE / "docs" / "sources" / "catalogue.csv"
USER_AGENT = "BIE-sources/0.1 (+https://github.com/hachi100/bieproject)"


def nom_de_fichier(identifiant: str, url: str, content_disposition: str | None) -> str:
    if content_disposition:
        match = re.search(r'filename="?([^";]+)"?', content_disposition)
        if match:
            return f"{identifiant}{Path(match.group(1)).suffix.lower()}"
    suffixe = Path(url.split("?")[0]).suffix.lower()
    return f"{identifiant}{suffixe if suffixe and len(suffixe) <= 5 else '.html'}"


def telecharger(url: str, contexte: ssl.SSLContext) -> tuple[bytes, str | None]:
    requete = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(requete, context=contexte, timeout=300) as reponse:
        return reponse.read(), reponse.headers.get("Content-Disposition")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dossier", type=Path, default=RACINE / "data" / "sources")
    parser.add_argument("--id", action="append", dest="ids", help="Identifiant(s) du catalogue à télécharger")
    parser.add_argument("--ca-bundle", type=Path, help="Magasin de certificats à utiliser (PEM)")
    args = parser.parse_args()

    contexte = ssl.create_default_context(cafile=str(args.ca_bundle) if args.ca_bundle else None)
    args.dossier.mkdir(parents=True, exist_ok=True)
    entrees = list(csv.DictReader(CATALOGUE.open(encoding="utf-8")))
    if args.ids:
        entrees = [e for e in entrees if e["id"] in set(args.ids)]

    echecs = 0
    with (args.dossier / "empreintes.sha256").open("a", encoding="utf-8") as empreintes:
        for entree in entrees:
            try:
                contenu, disposition = telecharger(entree["url"], contexte)
            except Exception as erreur:  # noqa: BLE001 — on continue et on résume à la fin
                print(f"ÉCHEC  {entree['id']:<28} {erreur}")
                echecs += 1
                continue
            fichier = args.dossier / nom_de_fichier(entree["id"], entree["url"], disposition)
            fichier.write_bytes(contenu)
            empreinte = hashlib.sha256(contenu).hexdigest()
            empreintes.write(f"{empreinte}  {fichier.name}  {entree['url']}\n")
            print(f"OK     {entree['id']:<28} {len(contenu) / 1_048_576:6.1f} Mo  {fichier.name}")
            time.sleep(0.5)  # courtoisie envers les serveurs publics
    print(f"\n{len(entrees) - echecs} document(s) téléchargé(s), {echecs} échec(s), dans {args.dossier}")
    return 1 if echecs else 0


if __name__ == "__main__":
    sys.exit(main())
