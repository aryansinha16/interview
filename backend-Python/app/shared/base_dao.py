from . import db
from .errors import NotFoundError


class BaseDAO:
    db_path: str
    label: str

    def __init__(self) -> None:
        db.ensure_db_file(self.db_path)

    def insert(self, record: dict) -> dict:
        records = db.read_records(self.db_path)
        records.append(record)
        db.write_records(self.db_path, records)
        return record

    def find_by_id(self, record_id: str) -> dict:
        for record in db.read_records(self.db_path):
            if record["id"] == record_id:
                return record
        raise NotFoundError(f"{self.label} {record_id} not found")

    def update(self, record_id: str, changes: dict) -> dict:
        records = db.read_records(self.db_path)
        for record in records:
            if record["id"] == record_id:
                record.update(changes)
                db.write_records(self.db_path, records)
                return record
        raise NotFoundError(f"{self.label} {record_id} not found")

    def delete(self, record_id: str) -> None:
        records = db.read_records(self.db_path)
        remaining = [record for record in records if record["id"] != record_id]
        if len(remaining) == len(records):
            raise NotFoundError(f"{self.label} {record_id} not found")
        db.write_records(self.db_path, remaining)

    def find_all(self, **filters: str) -> list[dict]:
        matches = [
            record
            for record in db.read_records(self.db_path)
            if all(record[field] == value for field, value in filters.items())
        ]
        return matches[::-1]

    def find_one(self, **filters: str) -> dict:
        matches = self.find_all(**filters)
        if not matches:
            raise NotFoundError(
                f"no {self.label} for {'/'.join(str(value) for value in filters.values())}"
            )
        return matches[0]
