import assert from "node:assert/strict";
import { test } from "node:test";
import { request, unique } from "../../support/server.js";

test("POST /api/v1/rotations returns the recomputed survivor", async () => {
  const result = await request("POST", "/api/v1/rotations", { pool_size: 5, rotation_step: 2 });
  assert.equal(result.status, 201);
  assert.deepEqual(Object.keys(result.body).sort(), [
    "computed_at",
    "id",
    "pool_size",
    "rotation_step",
    "survivor_server_id",
  ]);
  assert.match(result.body.id, /^rot_[0-9a-f]{6}$/);
  assert.equal(result.body.pool_size, 5);
  assert.equal(result.body.rotation_step, 2);
  assert.equal(result.body.survivor_server_id, 3);
  assert.match(result.body.computed_at, /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/);
});

test("rotation read / recompute / delete lifecycle", async () => {
  const created = await request("POST", "/api/v1/rotations", { pool_size: 5, rotation_step: 2 });
  const { id } = created.body;

  const fetched = await request("GET", `/api/v1/rotations/${id}`);
  assert.equal(fetched.status, 200);
  assert.deepEqual(fetched.body, created.body);

  const updated = await request("PUT", `/api/v1/rotations/${id}`, { pool_size: 7, rotation_step: 3 });
  assert.equal(updated.status, 200);
  assert.equal(updated.body.id, id);
  assert.equal(updated.body.pool_size, 7);
  assert.equal(updated.body.rotation_step, 3);
  assert.equal(updated.body.survivor_server_id, 4);

  const deleted = await request("DELETE", `/api/v1/rotations/${id}`);
  assert.equal(deleted.status, 204);
  assert.equal(deleted.body, null);

  const gone = await request("GET", `/api/v1/rotations/${id}`);
  assert.equal(gone.status, 404);
  assert.match(gone.body.error, new RegExp(`${id} not found`));
});

test("a missing rotation id gives 404 on GET, PUT and DELETE", async () => {
  for (const method of ["GET", "PUT", "DELETE"]) {
    const body = method === "PUT" ? { pool_size: 5, rotation_step: 2 } : undefined;
    const result = await request(method, "/api/v1/rotations/rot_missing", body);
    assert.equal(result.status, 404);
    assert.match(result.body.error, /rot_missing not found/);
  }
});

test("the seed rotation is retrievable", async () => {
  const result = await request("GET", "/api/v1/rotations/rot_8f21ac");
  assert.equal(result.status, 200);
  assert.deepEqual(result.body, {
    id: "rot_8f21ac",
    pool_size: 5,
    rotation_step: 2,
    survivor_server_id: 3,
    computed_at: "2026-01-14T10:02:00Z",
  });
});
