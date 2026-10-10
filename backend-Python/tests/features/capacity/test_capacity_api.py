def test_capacity_simulation_and_history(client):
    payload = {
        "customer_id": "cust_4821",
        "cost_deltas": [4, 2, 7, 6, 9, 14, 4, 1],
        "spend_budget": 10,
        "waiver_credits": 2,
    }
    response = client.post("/api/v1/capacity/simulate", json=payload)
    assert response.status_code == 201
    body = response.json()
    assert set(body) == {"customer_id", "cost_deltas", "spend_budget", "waiver_credits", "furthest_month"}
    assert body["furthest_month"] == 4

    history = client.get("/api/v1/capacity/cust_4821/simulations")
    assert history.status_code == 200
    assert len(history.json()) == 2
    assert history.json()[0] == body
    assert all("id" not in record for record in history.json())

    assert client.get("/api/v1/capacity/cust_unknown/simulations").json() == []
