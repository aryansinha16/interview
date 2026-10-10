import { newId, nowIso } from "../../shared/db.js";
import { RotationDao } from "./dao.js";

const dao = new RotationDao();

export function findSurvivor(poolSize, rotationStep) {
  // TODO: implement by interviewee
  throw new Error("findSurvivor not implemented");
}

export function createRotation(body) {
  return dao.insert({
    id: newId("rot"),
    pool_size: body.pool_size,
    rotation_step: body.rotation_step,
    survivor_server_id: findSurvivor(body.pool_size, body.rotation_step),
    computed_at: nowIso(),
  });
}

export function getRotation(id) {
  return dao.findById(id);
}

export function updateRotation(id, body) {
  return dao.update(id, {
    pool_size: body.pool_size,
    rotation_step: body.rotation_step,
    survivor_server_id: findSurvivor(body.pool_size, body.rotation_step),
    computed_at: nowIso(),
  });
}

export function deleteRotation(id) {
  return dao.delete(id);
}
