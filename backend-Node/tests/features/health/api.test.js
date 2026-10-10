import assert from "node:assert/strict";
import { test } from "node:test";
import { request } from "../../support/server.js";

test("GET /health reports the node backend", async () => {
  const result = await request("GET", "/health");
  assert.equal(result.status, 200);
  assert.deepEqual(result.body, { status: "ok", backend: "node" });
});
