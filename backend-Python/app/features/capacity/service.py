from app.shared.db import new_id

from .dao import CapacitySimulationDAO
from .dto import CapacitySimulationRequest, CapacitySimulationResponse


def furthest_month(cost_deltas: list[int], compute_budget: int, reserved_credits: int) -> int:
    # TODO: implement by interviewee
    raise NotImplementedError


class CapacityPlannerService:
    def __init__(self, dao: CapacitySimulationDAO) -> None:
        self.dao = dao

    def simulate(self, request: CapacitySimulationRequest) -> CapacitySimulationResponse:
        record = self.dao.insert(
            {
                "id": new_id("sim"),
                "customer_id": request.customer_id,
                "cost_deltas": request.cost_deltas,
                "spend_budget": request.spend_budget,
                "waiver_credits": request.waiver_credits,
                "furthest_month": furthest_month(
                    request.cost_deltas, request.spend_budget, request.waiver_credits
                ),
            }
        )
        return self._to_response(record)

    def history(self, customer_id: str) -> list[CapacitySimulationResponse]:
        return [self._to_response(record) for record in self.dao.find_all(customer_id=customer_id)]

    @staticmethod
    def _to_response(record: dict) -> CapacitySimulationResponse:
        return CapacitySimulationResponse(
            customer_id=record["customer_id"],
            cost_deltas=record["cost_deltas"],
            spend_budget=record["spend_budget"],
            waiver_credits=record["waiver_credits"],
            furthest_month=record["furthest_month"],
        )
