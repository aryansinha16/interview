from pydantic import BaseModel, ConfigDict, Field


class CapacitySimulationRequest(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")

    customer_id: str
    cost_deltas: list[int]
    spend_budget: int = Field(ge=0)
    waiver_credits: int = Field(ge=0)


class CapacitySimulationResponse(BaseModel):
    # TODO: implement by interviewee
    pass
