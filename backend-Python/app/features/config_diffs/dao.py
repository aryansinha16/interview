from app.shared.base_dao import BaseDAO


class ConfigDiffDAO(BaseDAO):
    db_path = "config_drift_logs_db.json"
    label = "config diff"
