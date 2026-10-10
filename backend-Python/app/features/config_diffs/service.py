import difflib

from app.shared.db import new_id

from .dao import ConfigDiffDAO
from .dto import ConfigDiffRequest, ConfigDiffResponse


def compute_drift(source: str, target: str) -> int:
    # TODO: implement by interviewee
    raise NotImplementedError


def build_diff_details(source: str, target: str) -> list[dict]:
    source_lines, target_lines = source.splitlines(), target.splitlines()
    details = []
    for tag, i1, i2, j1, j2 in difflib.SequenceMatcher(None, source_lines, target_lines).get_opcodes():
        if tag == "equal":
            continue
        changed = target_lines[j1:j2] or source_lines[i1:i2]
        details.append({"op": tag, "line": changed[0].split("=")[0]})
    return details


class ConfigDiffService:
    def __init__(self, dao: ConfigDiffDAO) -> None:
        self.dao = dao

    def diff(self, request: ConfigDiffRequest) -> ConfigDiffResponse:
        record = self.dao.insert(
            {
                "id": new_id("diff"),
                "config_id": request.config_id,
                "source_env": request.source_env,
                "target_env": request.target_env,
                "drift_score": compute_drift(request.source_config, request.target_config),
                "diff_details": build_diff_details(request.source_config, request.target_config),
            }
        )
        return self._to_response(record)

    def history(self, config_id: str) -> list[ConfigDiffResponse]:
        return [self._to_response(record) for record in self.dao.find_all(config_id=config_id)]

    @staticmethod
    def _to_response(record: dict) -> ConfigDiffResponse:
        return ConfigDiffResponse(
            config_id=record["config_id"],
            source_env=record["source_env"],
            target_env=record["target_env"],
            drift_score=record["drift_score"],
            diff_details=record["diff_details"],
        )
