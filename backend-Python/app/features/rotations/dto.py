from pydantic import BaseModel, ConfigDict, Field


class RotationRequest(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")

    pool_size: int = Field(ge=2)
    rotation_step: int = Field(ge=1)


class RotationResponse(BaseModel):
    # TODO: implement by interviewee
    pass
