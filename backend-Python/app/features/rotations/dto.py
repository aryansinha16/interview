from pydantic import BaseModel, ConfigDict, Field


class RotationRequest(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")

    pool_size: int = Field(ge=2)
    rotation_step: int = Field(ge=1)

        # "id": "rot_8f21ac",
        # "pool_size": 5,
        # "rotation_step": 2,
        # "survivor_server_id": 3,
        # "computed_at": "2026-01-14T10:02:00Z",


class RotationResponse(BaseModel):
    id: str
    pool_size: int
    rotation_step: int
    survivor_server_id: int
    computed_at: str