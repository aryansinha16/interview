import { Router } from "express";
import { validateBody } from "../../shared/validation.js";
import { requestSchema, toResponse } from "./dto.js";
import { diffConfigs, listConfigDiffs } from "./service.js";

export function configDiffsRouter() {
  const router = Router();

  router.post("/configs/diff", async (req, res) => {
    const body = validateBody(requestSchema, req.body);
    const record = await diffConfigs(body);
    res.status(201).json(toResponse(record));
  });

  router.get("/configs/:config_id/diffs", async (req, res) => {
    const records = await listConfigDiffs(req.params.config_id);
    res.json(records.map(toResponse));
  });

  return router;
}
