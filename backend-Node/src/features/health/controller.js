import { Router } from "express";

export function healthRouter() {
  const router = Router();

  router.get("/health", (req, res) => {
    res.json({ status: "ok", backend: "node" });
  });

  return router;
}
