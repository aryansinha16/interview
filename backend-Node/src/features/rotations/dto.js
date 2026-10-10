import { z } from "zod";

export const requestSchema = z.strictObject({
  pool_size: z.int().min(2),
  rotation_step: z.int().min(1),
});

export function toResponse(record) {
  // TODO: implement by interviewee
  throw new Error("toResponse not implemented");
}
