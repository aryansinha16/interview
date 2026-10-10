import assert from "node:assert/strict";
import { test } from "node:test";
import { request } from "../support/server.js";

const VALIDATION_CASES = [
  {
    name: "pool_size below the minimum",
    path: "/api/v1/rotations",
    payload: { pool_size: 1, rotation_step: 2 },
    field: "pool_size",
    message: "Too small: expected number to be >=2",
  },
  {
    name: "numeric string in an integer field",
    path: "/api/v1/rotations",
    payload: { pool_size: "5", rotation_step: 2 },
    field: "pool_size",
    message: "Invalid input: expected number, received string",
  },
  {
    name: "float in an integer field",
    path: "/api/v1/rotations",
    payload: { pool_size: 5.5, rotation_step: 2 },
    field: "pool_size",
    message: "Invalid input: expected int, received number",
  },
  {
    name: "unknown extra field",
    path: "/api/v1/rotations",
    payload: { pool_size: 5, rotation_step: 2, unexpected: true },
    field: "unexpected",
    message: 'Unrecognized key: "unexpected"',
  },
  {
    name: "zero rotation_step",
    path: "/api/v1/rotations",
    payload: { pool_size: 5, rotation_step: 0 },
    field: "rotation_step",
    message: "Too small: expected number to be >=1",
  },
  {
    name: "empty rotation body",
    path: "/api/v1/rotations",
    payload: {},
    field: "pool_size",
    message: "Invalid input: expected number, received undefined",
  },
  {
    name: "missing customer_id",
    path: "/api/v1/capacity/simulate",
    payload: { cost_deltas: [1, 2], spend_budget: 1, waiver_credits: 0 },
    field: "customer_id",
    message: "Invalid input: expected string, received undefined",
  },
  {
    name: "missing target_config",
    path: "/api/v1/configs/diff",
    payload: {
      config_id: "cfg_x",
      source_env: "staging",
      target_env: "production",
      source_config: "a=1",
    },
    field: "target_config",
    message: "Invalid input: expected string, received undefined",
  },
  {
    name: "negative spend_budget",
    path: "/api/v1/capacity/simulate",
    payload: {
      customer_id: "cust_x",
      cost_deltas: [1, 2],
      spend_budget: -1,
      waiver_credits: 0,
    },
    field: "spend_budget",
    message: "Too small: expected number to be >=0",
  },
  {
    name: "non-numeric waiver_credits",
    path: "/api/v1/capacity/simulate",
    payload: {
      customer_id: "cust_x",
      cost_deltas: [1, 2],
      spend_budget: 1,
      waiver_credits: "x",
    },
    field: "waiver_credits",
    message: "Invalid input: expected number, received string",
  },
  {
    name: "non-integer element inside cost_deltas",
    path: "/api/v1/capacity/simulate",
    payload: {
      customer_id: "cust_x",
      cost_deltas: [4, "2"],
      spend_budget: 1,
      waiver_credits: 0,
    },
    field: "cost_deltas.1",
    message: "Invalid input: expected number, received string",
  },
];

for (const testCase of VALIDATION_CASES) {
  test(`422 from Zod: ${testCase.name}`, async () => {
    const result = await request("POST", testCase.path, testCase.payload);
    assert.equal(result.status, 422);
    assert.equal(result.body.error, "Validation failed");
    for (const detail of result.body.details) {
      assert.deepEqual(Object.keys(detail).sort(), ["field", "message"]);
    }
    assert.ok(
      result.body.details.some(
        (detail) => detail.field === testCase.field && detail.message === testCase.message,
      ),
      `expected ${testCase.field}: ${testCase.message} in ${JSON.stringify(result.body.details)}`,
    );
  });
}

test("a request without a JSON body gives 422 from Zod", async () => {
  const result = await request("POST", "/api/v1/rotations");
  assert.equal(result.status, 422);
  assert.deepEqual(result.body, {
    error: "Validation failed",
    details: [{ field: "body", message: "Invalid input: expected object, received undefined" }],
  });
});

test("unparseable JSON gives 422 with a single body detail", async () => {
  const result = await request("POST", "/api/v1/rotations", '{"pool_size": 5,');
  assert.equal(result.status, 422);
  assert.equal(result.body.error, "Validation failed");
  assert.equal(result.body.details.length, 1);
  assert.equal(result.body.details[0].field, "body");
  assert.equal(typeof result.body.details[0].message, "string");
});
