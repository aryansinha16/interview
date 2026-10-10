import { z } from "zod";

export const requestSchema = z.strictObject({
  customer_id: z.string(),
  cost_deltas: z.array(z.int()),
  spend_budget: z.int().min(0),
  waiver_credits: z.int().min(0),
});

export function toResponse(record) {
  // TODO: implement by interviewee
  throw new Error("toResponse not implemented");
}
