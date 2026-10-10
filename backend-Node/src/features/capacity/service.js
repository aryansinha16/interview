import { newId } from "../../shared/db.js";
import { CapacitySimulationDao } from "./dao.js";

const dao = new CapacitySimulationDao();

export function furthestMonth(costDeltas, computeBudget, reservedCredits) {
  // TODO: implement by interviewee
  throw new Error("furthestMonth not implemented");
}

export function simulateCapacity(body) {
  return dao.insert({
    id: newId("sim"),
    customer_id: body.customer_id,
    cost_deltas: body.cost_deltas,
    spend_budget: body.spend_budget,
    waiver_credits: body.waiver_credits,
    furthest_month: furthestMonth(body.cost_deltas, body.spend_budget, body.waiver_credits),
  });
}

export function listCapacitySimulations(customerId) {
  return dao.findAllNewestFirst({ customer_id: customerId });
}
