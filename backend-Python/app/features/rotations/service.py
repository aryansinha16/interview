from app.shared.db import new_id, now_iso

from .dao import RotationDAO
from .dto import RotationRequest, RotationResponse


def find_survivor(pool_size: int, rotation_step: int) -> int:
    # TODO: implement by interviewee
    raise NotImplementedError


class RotationService:
    def __init__(self, dao: RotationDAO) -> None:
        self.dao = dao

    def create(self, request: RotationRequest) -> RotationResponse:
        record = self.dao.insert(
            {
                "id": new_id("rot"),
                "pool_size": request.pool_size,
                "rotation_step": request.rotation_step,
                "survivor_server_id": find_survivor(request.pool_size, request.rotation_step),
                "computed_at": now_iso(),
            }
        )
        return RotationResponse(**record)

    def get(self, rotation_id: str) -> RotationResponse:
        return RotationResponse(**self.dao.find_by_id(rotation_id))

    def update(self, rotation_id: str, request: RotationRequest) -> RotationResponse:
        record = self.dao.update(
            rotation_id,
            {
                "pool_size": request.pool_size,
                "rotation_step": request.rotation_step,
                "survivor_server_id": find_survivor(request.pool_size, request.rotation_step),
                "computed_at": now_iso(),
            },
        )
        return RotationResponse(**record)

    def delete(self, rotation_id: str) -> None:
        self.dao.delete(rotation_id)
