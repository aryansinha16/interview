from pydantic import BaseModel, ConfigDict


class ConfigDiffRequest(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")

    config_id: str
    source_env: str
    target_env: str
    source_config: str
    target_config: str


class ConfigDiffResponse(BaseModel):
    # TODO: implement by interviewee
    pass
