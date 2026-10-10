import { Router } from "express";
import { validateBody } from "../../shared/validation.js";
import { requestSchema, toResponse } from "./dto.js";
import { listCapacitySimulations, simulateCapacity } from "./service.js";

export function capacityRouter() {
  const router = Router();

  router.post("/capacity/simulate", async (req, res) => {
    const body = validateBody(requestSchema, req.body);
    const record = await simulateCapacity(body);
    res.status(201).json(toResponse(record));
  });

  router.get("/capacity/:customer_id/simulations", async (req, res) => {
    const records = await listCapacitySimulations(req.params.customer_id);
    res.json(records.map(toResponse));
  });

  return router;
}
