from fastapi import APIRouter, Response

from .dao import RotationDAO
from .dto import RotationRequest
from .service import RotationService

router = APIRouter(prefix="/api/v1")

rotation_service = RotationService(RotationDAO())


@router.post("/rotations", status_code=201)
def create_rotation(body: RotationRequest):
    return rotation_service.create(body).model_dump()


@router.get("/rotations/{rotation_id}")
def get_rotation(rotation_id: str):
    return rotation_service.get(rotation_id).model_dump()


@router.put("/rotations/{rotation_id}")
def update_rotation(rotation_id: str, body: RotationRequest):
    return rotation_service.update(rotation_id, body).model_dump()


@router.delete("/rotations/{rotation_id}", status_code=204, response_class=Response)
def delete_rotation(rotation_id: str):
    rotation_service.delete(rotation_id)
    return Response(status_code=204)
