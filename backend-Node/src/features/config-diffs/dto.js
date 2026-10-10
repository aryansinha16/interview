import { z } from "zod";

export const requestSchema = z.strictObject({
  config_id: z.string(),
  source_env: z.string(),
  target_env: z.string(),
  source_config: z.string(),
  target_config: z.string(),
});

export function toResponse(record) {
  // TODO: implement by interviewee
  throw new Error("toResponse not implemented");
}
