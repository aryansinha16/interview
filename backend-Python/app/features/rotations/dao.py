from app.shared.base_dao import BaseDAO


class RotationDAO(BaseDAO):
    db_path = "server_rotation_db.json"
    label = "rotation"
