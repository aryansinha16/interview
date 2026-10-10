# OptiFlow — Backend Engineering Interview Task

**Time budget:** 90–120 minutes. **Deliverable:** a running REST service, its tests, and a README
with the commands to run both, implemented in **one** of the three language tracks below.

## Scenario

OptiFlow is a SaaS platform that DevOps teams use to manage cloud resources, automate scheduling and
optimize infrastructure costs. It serves 200+ enterprise customers. In production the business logic
lives in a **service layer** and all data is persisted in PostgreSQL.

For this exercise you replace PostgreSQL with **one JSON file per feature** on the disk. Everything else
— layering, validation, error handling — must look like production code.

## Your task

Implement three OptiFlow features behind a single REST API.

| # | Feature                      | Endpoints                                                                          |
|---|------------------------------|------------------------------------------------------------------------------------|
| 1 | Round-Robin Load Balancer    | `POST/GET/PUT/DELETE /api/v1/rotations[/{id}]`                                     |
| 2 | Configuration Drift Detector | `POST /api/v1/configs/diff`, `GET /api/v1/configs/{config_id}/diffs`               |
| 3 | Capacity Planning Simulator  | `POST /api/v1/capacity/simulate`, `GET /api/v1/capacity/{customer_id}/simulations` |

### Task 1 — Round-Robin Load Balancer

A pool of *n* application servers sits in a rotation ring. Servers are removed from the ring as they
hit their connection limit, and every *k*-th request in the rotation decides which server is removed
next. The last server left in the ring handles all overflow traffic.

- **Input:** `pool_size` (integer, must be **greater than 1**) and `rotation_step` (integer, must be **greater than 0**).
- **Output:** `survivor_server_id` — the 1-indexed id of the server that survives the elimination sequence.
- **Worked examples:** `pool_size=5, rotation_step=2` → survivor `3`; `pool_size=7, rotation_step=3` → survivor `4`.
- **CRUD:** create (201), read by id (200), update — recompute for new values of n and k (200), delete (204, empty body).

### Task 2 — Configuration Drift Detector

OptiFlow monitors infrastructure-as-code configs across environments. When a staging config drifts
from production, the platform must report how far apart the two configs are, so engineers can see
exactly what changed.

- **Input:** `config_id`, `source_env`, `target_env`, `source_config`, `target_config` (all required strings).
- **Output:** `drift_score` — the minimum number of single-character insertions, deletions and substitutions needed to turn the source config into the target config.
- **Worked example:** source `server_port=8080\ntimeout=30` → target `server_port=9090\ntimeout=60\nssl=true`. Reconcile them by hand first and write down what you get; the acceptance criteria will check that the number your service returns is the one your own reasoning produces from the definition above.
- **History read:** all past diffs for one `config_id`, **newest first**.

### Task 3 — Capacity Planning Simulator

Given a sequence of monthly cost increases, a spend budget (compute spend you can absorb) and
a number of waiver credits (reserved instances you can spend instead), OptiFlow must tell the
customer how many months they can sustain before exceeding their plan.

- **Input:** `customer_id`, `cost_deltas` (array of integers, may be empty), `spend_budget` (integer ≥ 0), `waiver_credits` (integer ≥ 0).
- **Output:** `furthest_month` — the furthest month index the customer can reach (0 if they cannot pass the first increase, `len(cost_deltas)` if they reach the end).
- **Worked example:** `cost_deltas=[4,2,7,6,9,14,4,1]`, `spend_budget=10`, `waiver_credits=2` → `furthest_month=4`.
- **History read:** a customer's past simulations, **newest first**.

## The API contract (must match exactly)

Base path `/api/v1`. Flat JSON bodies — **no** `data`/`meta` envelope, and no extra fields.

### Request bodies

`POST /api/v1/rotations`

```json
{
  "pool_size": 5,
  "rotation_step": 2
}
```

`PUT /api/v1/rotations/{id}`

```json
{
  "pool_size": 7,
  "rotation_step": 3
}
```

`POST /api/v1/configs/diff`

```json
{
  "config_id": "cfg_staging_7",
  "source_env": "staging",
  "target_env": "production",
  "source_config": "server_port=8080\ntimeout=30",
  "target_config": "server_port=9090\ntimeout=60\nssl=true"
}
```

`POST /api/v1/capacity/simulate`

```json
{
  "customer_id": "cust_4821",
  "cost_deltas": [
    4,
    2,
    7,
    6,
    9,
    14,
    4,
    1
  ],
  "spend_budget": 10,
  "waiver_credits": 2
}
```

### Response bodies

`RotationResponse`

```json
{
  "id": "rot_8f21ac",
  "pool_size": 5,
  "rotation_step": 2,
  "survivor_server_id": 3,
  "computed_at": "2026-01-14T10:02:00Z"
}
```

`ConfigDiffResponse`

```json
{
  "config_id": "cfg_staging_7",
  "source_env": "staging",
  "target_env": "production",
  "drift_score": 12
}
```

`CapacitySimulationResponse`

```json
{
  "customer_id": "cust_4821",
  "cost_deltas": [
    4,
    2,
    7,
    6,
    9,
    14,
    4,
    1
  ],
  "spend_budget": 10,
  "waiver_credits": 2,
  "furthest_month": 4
}
```

- `id` values look like `rot_8f21ac` (prefix + underscore + 6 lowercase hex characters).
- `computed_at` is ISO-8601 UTC with a trailing `Z`, e.g. `2026-01-14T10:02:00Z`.

### Status codes

| Method | Path                                         | Success               |
|--------|----------------------------------------------|-----------------------|
| POST   | `/api/v1/rotations`                          | 201                   |
| GET    | `/api/v1/rotations/{id}`                     | 200                   |
| PUT    | `/api/v1/rotations/{id}`                     | 200                   |
| DELETE | `/api/v1/rotations/{id}`                     | 204 (empty body)      |
| POST   | `/api/v1/configs/diff`                       | 201                   |
| GET    | `/api/v1/configs/{config_id}/diffs`          | 200 (array)           |
| POST   | `/api/v1/capacity/simulate`                  | 201                   |
| GET    | `/api/v1/capacity/{customer_id}/simulations` | 200 (array)           |
| GET    | `/health`                                    | 200 `{"status":"ok"}` |

### Errors

| Case                  | Status  | Body                                                                              |
|-----------------------|---------|-----------------------------------------------------------------------------------|
| Body fails validation | **422** | `{"error":"Validation failed","details":[{"field":"pool_size","message":"..."}]}` |
| Record not found      | **404** | `{"error":"<message>"}`                                                           |
| Malformed JSON        | **422** | same shape, one detail with field `body`                                          |
