from fastapi import APIRouter

from .dao import ConfigDiffDAO
from .dto import ConfigDiffRequest
from .service import ConfigDiffService

router = APIRouter(prefix="/api/v1")

config_diff_service = ConfigDiffService(ConfigDiffDAO())


@router.post("/configs/diff", status_code=201)
def diff_configs(body: ConfigDiffRequest):
    return config_diff_service.diff(body).model_dump()


@router.get("/configs/{config_id}/diffs")
def list_config_diffs(config_id: str):
    return [response.model_dump() for response in config_diff_service.history(config_id)]
