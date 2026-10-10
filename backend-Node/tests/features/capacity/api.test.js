import assert from "node:assert/strict";
import { test } from "node:test";
import { request, unique } from "../../support/server.js";

test("POST /api/v1/capacity/simulate reports furthest_month 4 and lists its history", async () => {
  const customerId = unique("cust_test");
  const payload = {
    customer_id: customerId,
    cost_deltas: [4, 2, 7, 6, 9, 14, 4, 1],
    spend_budget: 10,
    waiver_credits: 2,
  };
  const result = await request("POST", "/api/v1/capacity/simulate", payload);
  assert.equal(result.status, 201);
  assert.deepEqual(result.body, { ...payload, furthest_month: 4 });

  const history = await request("GET", `/api/v1/capacity/${customerId}/simulations`);
  assert.equal(history.status, 200);
  assert.deepEqual(history.body, [result.body]);
});

test("capacity history returns the newest record first", async () => {
  const customerId = unique("cust_test");
  const payload = {
    customer_id: customerId,
    cost_deltas: [1, 1, 1],
    spend_budget: 0,
    waiver_credits: 0,
  };
  const first = await request("POST", "/api/v1/capacity/simulate", payload);
  assert.equal(first.status, 201);
  assert.equal(first.body.furthest_month, 0);

  const second = await request("POST", "/api/v1/capacity/simulate", { ...payload, spend_budget: 3 });
  assert.equal(second.status, 201);
  assert.equal(second.body.furthest_month, 3);

  const history = await request("GET", `/api/v1/capacity/${customerId}/simulations`);
  assert.equal(history.status, 200);
  assert.deepEqual(history.body, [second.body, first.body]);
});
