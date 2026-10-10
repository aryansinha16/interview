import assert from "node:assert/strict";
import { test } from "node:test";
import { request, unique, SOURCE_CONFIG, TARGET_CONFIG } from "../../support/server.js";

test("POST /api/v1/configs/diff reports 12 with one diff entry and no id", async () => {
  const configId = unique("cfg_test");
  const result = await request("POST", "/api/v1/configs/diff", {
    config_id: configId,
    source_env: "staging",
    target_env: "production",
    source_config: SOURCE_CONFIG,
    target_config: TARGET_CONFIG,
  });
  assert.equal(result.status, 201);
  assert.deepEqual(result.body, {
    config_id: configId,
    source_env: "staging",
    target_env: "production",
    drift_score: 12,
    diff_details: [{ op: "replace", line: "server_port" }],
  });

  const history = await request("GET", `/api/v1/configs/${configId}/diffs`);
  assert.equal(history.status, 200);
  assert.deepEqual(history.body, [result.body]);
});

test("config diff history returns the newest record first", async () => {
  const configId = unique("cfg_test");
  const first = await request("POST", "/api/v1/configs/diff", {
    config_id: configId,
    source_env: "staging",
    target_env: "production",
    source_config: SOURCE_CONFIG,
    target_config: TARGET_CONFIG,
  });
  assert.equal(first.status, 201);

  const second = await request("POST", "/api/v1/configs/diff", {
    config_id: configId,
    source_env: "staging",
    target_env: "production",
    source_config: "a=1\nb=2\nc=3",
    target_config: "a=1\nc=3",
  });
  assert.equal(second.status, 201);
  assert.equal(second.body.drift_score, 4);

  const history = await request("GET", `/api/v1/configs/${configId}/diffs`);
  assert.equal(history.status, 200);
  assert.deepEqual(history.body, [second.body, first.body]);
});

test("empty strings are valid for the string fields", async () => {
  const result = await request("POST", "/api/v1/configs/diff", {
    config_id: "",
    source_env: "",
    target_env: "",
    source_config: "",
    target_config: "",
  });
  assert.equal(result.status, 201);
  assert.deepEqual(result.body, {
    config_id: "",
    source_env: "",
    target_env: "",
    drift_score: 0,
    diff_details: [],
  });
});
