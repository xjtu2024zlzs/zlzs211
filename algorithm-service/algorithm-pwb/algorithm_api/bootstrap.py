from __future__ import annotations

import sys
from pathlib import Path


API_ROOT = Path(__file__).resolve().parent
PROJECT_ROOT = API_ROOT.parent
MAGNETO_ROOT = PROJECT_ROOT / "algorithms" / "magneto"


def configure_paths() -> None:
    for path in (PROJECT_ROOT, MAGNETO_ROOT):
        path_text = str(path)
        if path_text not in sys.path:
            sys.path.insert(0, path_text)


configure_paths()

