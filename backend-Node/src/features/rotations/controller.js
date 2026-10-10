import { Router } from "express";
import { validateBody } from "../../shared/validation.js";
import { requestSchema, toResponse } from "./dto.js";
import { createRotation, deleteRotation, getRotation, updateRotation } from "./service.js";

export function rotationsRouter() {
  const router = Router();

  router.post("/rotations", async (req, res) => {
    const body = validateBody(requestSchema, req.body);
    const record = await createRotation(body);
    res.status(201).json(toResponse(record));
  });

  router.get("/rotations/:id", async (req, res) => {
    const record = await getRotation(req.params.id);
    res.json(toResponse(record));
  });

  router.put("/rotations/:id", async (req, res) => {
    const body = validateBody(requestSchema, req.body);
    const record = await updateRotation(req.params.id, body);
    res.json(toResponse(record));
  });

  router.delete("/rotations/:id", async (req, res) => {
    await deleteRotation(req.params.id);
    res.status(204).end();
  });

  return router;
}
