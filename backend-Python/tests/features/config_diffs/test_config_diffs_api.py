def test_config_diff_and_history(client):
    payload = {
        "config_id": "cfg_staging_7",
        "source_env": "staging",
        "target_env": "production",
        "source_config": "server_port=8080\ntimeout=30",
        "target_config": "server_port=9090\ntimeout=60\nssl=true",
    }
    response = client.post("/api/v1/configs/diff", json=payload)
    assert response.status_code == 201
    body = response.json()
    assert set(body) == {"config_id", "source_env", "target_env", "drift_score", "diff_details"}
    assert body["drift_score"] == 12
    assert body["diff_details"] == [{"op": "replace", "line": "server_port"}]

    history = client.get("/api/v1/configs/cfg_staging_7/diffs")
    assert history.status_code == 200
    records = history.json()
    assert len(records) == 2
    assert records[0] == body
    assert records[1]["target_env"] == "production"
    assert all("id" not in record for record in records)

    assert client.get("/api/v1/configs/cfg_unknown/diffs").json() == []


def test_config_diff_allows_empty_strings(client):
    response = client.post(
        "/api/v1/configs/diff",
        json={"config_id": "", "source_env": "", "target_env": "", "source_config": "", "target_config": ""},
    )
    assert response.status_code == 201
    assert response.json()["drift_score"] == 0
    assert response.json()["diff_details"] == []
