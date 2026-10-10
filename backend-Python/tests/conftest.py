import shutil

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.shared import db


@pytest.fixture()
def client(tmp_path, monkeypatch):
    for path in db.DATA_DIR.glob("*.json"):
        shutil.copy(path, tmp_path / path.name)
    monkeypatch.setattr(db, "DATA_DIR", tmp_path)
    with TestClient(app) as test_client:
        yield test_client
