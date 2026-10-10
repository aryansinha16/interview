def test_validation_failures(client):
    cases = [
        ("/api/v1/rotations", {"pool_size": 1, "rotation_step": 2}, "pool_size"),
        ("/api/v1/rotations", {"pool_size": 5, "rotation_step": 0}, "rotation_step"),
        ("/api/v1/rotations", {"pool_size": "5", "rotation_step": 2}, "pool_size"),
        ("/api/v1/rotations", {"pool_size": 5.5, "rotation_step": 2}, "pool_size"),
        ("/api/v1/configs/diff", {"config_id": "cfg_1"}, "source_env"),
        (
            "/api/v1/capacity/simulate",
            {"customer_id": "c", "cost_deltas": [], "spend_budget": -1, "waiver_credits": 0},
            "spend_budget",
        ),
        (
            "/api/v1/capacity/simulate",
            {"customer_id": "c", "cost_deltas": [], "spend_budget": 0, "waiver_credits": "x"},
            "waiver_credits",
        ),
        (
            "/api/v1/capacity/simulate",
            {"customer_id": "c", "cost_deltas": [1, "2"], "spend_budget": 0, "waiver_credits": 0},
            "cost_deltas.1",
        ),
    ]
    for path, payload, field in cases:
        response = client.post(path, json=payload)
        assert response.status_code == 422, (path, payload, response.text)
        body = response.json()
        assert body["error"] == "Validation failed"
        assert all(set(detail) == {"field", "message"} for detail in body["details"])
        assert field in [detail["field"] for detail in body["details"]], (path, payload, body)


def test_unparseable_json_is_422(client):
    response = client.post("/api/v1/rotations", content="{not json", headers={"Content-Type": "application/json"})
    assert response.status_code == 422
    body = response.json()
    assert body["error"] == "Validation failed"
    assert body["details"] == [{"field": "body", "message": "JSON decode error"}]


def test_unknown_route_is_404(client):
    assert client.get("/api/v1/nope").status_code == 404
