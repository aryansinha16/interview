from fastapi import APIRouter

from .dao import CapacitySimulationDAO
from .dto import CapacitySimulationRequest
from .service import CapacityPlannerService

router = APIRouter(prefix="/api/v1")

capacity_planner_service = CapacityPlannerService(CapacitySimulationDAO())


@router.post("/capacity/simulate", status_code=201)
def simulate_capacity(body: CapacitySimulationRequest):
    return capacity_planner_service.simulate(body).model_dump()


@router.get("/capacity/{customer_id}/simulations")
def list_capacity_simulations(customer_id: str):
    return [response.model_dump() for response in capacity_planner_service.history(customer_id)]
