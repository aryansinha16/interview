import json
import secrets
from datetime import datetime, timezone
from pathlib import Path

DATA_DIR = Path(__file__).resolve().parents[2] / "data"


def new_id(prefix: str) -> str:
    return f"{prefix}_{secrets.token_hex(3)}"


def now_iso() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def ensure_db_file(filename: str) -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    path = DATA_DIR / filename
    if not path.exists():
        path.write_text(json.dumps([], indent=2) + "\n", encoding="utf-8")


def read_records(filename: str) -> list[dict]:
    with open(DATA_DIR / filename, encoding="utf-8") as handle:
        return json.load(handle)


def write_records(filename: str, records: list[dict]) -> None:
    with open(DATA_DIR / filename, "w", encoding="utf-8") as handle:
        json.dump(records, handle, indent=2)
