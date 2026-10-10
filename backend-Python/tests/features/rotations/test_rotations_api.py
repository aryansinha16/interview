ROTATION_FIELDS = {"id", "pool_size", "rotation_step", "survivor_server_id", "computed_at"}


def test_seed_rotation_is_retrievable(client):
    response = client.get("/api/v1/rotations/rot_8f21ac")
    assert response.status_code == 200
    assert response.json() == {
        "id": "rot_8f21ac",
        "pool_size": 5,
        "rotation_step": 2,
        "survivor_server_id": 3,
        "computed_at": "2026-01-14T10:02:00Z",
    }


def test_rotation_crud(client):
    created = client.post("/api/v1/rotations", json={"pool_size": 5, "rotation_step": 2})
    assert created.status_code == 201
    body = created.json()
    assert set(body) == ROTATION_FIELDS
    assert body["survivor_server_id"] == 3
    assert body["id"].startswith("rot_")
    assert len(body["id"]) == len("rot_") + 6

    fetched = client.get(f"/api/v1/rotations/{body['id']}")
    assert fetched.status_code == 200
    assert fetched.json() == body

    updated = client.put(f"/api/v1/rotations/{body['id']}", json={"pool_size": 7, "rotation_step": 3})
    assert updated.status_code == 200
    assert set(updated.json()) == ROTATION_FIELDS
    assert updated.json()["id"] == body["id"]
    assert updated.json()["survivor_server_id"] == 4

    deleted = client.delete(f"/api/v1/rotations/{body['id']}")
    assert deleted.status_code == 204
    assert deleted.content == b""

    assert client.get(f"/api/v1/rotations/{body['id']}").status_code == 404


def test_missing_rotation_is_404(client):
    responses = [
        client.get("/api/v1/rotations/rot_000000"),
        client.put("/api/v1/rotations/rot_000000", json={"pool_size": 2, "rotation_step": 1}),
        client.delete("/api/v1/rotations/rot_000000"),
    ]
    for response in responses:
        assert response.status_code == 404
        assert response.json() == {"error": "rotation rot_000000 not found"}
