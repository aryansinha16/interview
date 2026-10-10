package com.optiflow.shared;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BaseDao {

    protected final JsonDb db;
    private final String fileName;

    protected BaseDao(JsonDb db, String fileName) {
        this.db = db;
        this.fileName = fileName;
        db.ensureFile(fileName);
    }

    public Map<String, Object> insert(Map<String, Object> record) {
        List<Map<String, Object>> records = db.read(fileName);
        records.add(record);
        db.write(fileName, records);
        return record;
    }

    public Map<String, Object> findById(String id) {
        return db.read(fileName).stream()
                .filter(record -> id.equals(record.get("id")))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(fileName + ": record " + id + " not found"));
    }

    public List<Map<String, Object>> findAll(Map<String, Object> filters) {
        return db.read(fileName).stream()
                .filter(record -> filters.entrySet().stream()
                        .allMatch(filter -> Objects.equals(record.get(filter.getKey()), filter.getValue())))
                .toList();
    }

    public Map<String, Object> update(String id, Map<String, Object> fields) {
        List<Map<String, Object>> records = db.read(fileName);
        for (Map<String, Object> record : records) {
            if (id.equals(record.get("id"))) {
                record.putAll(fields);
                db.write(fileName, records);
                return record;
            }
        }
        throw new NotFoundException(fileName + ": record " + id + " not found");
    }

    public void delete(String id) {
        List<Map<String, Object>> records = db.read(fileName);
        List<Map<String, Object>> remaining = records.stream()
                .filter(record -> !id.equals(record.get("id")))
                .toList();
        if (remaining.size() == records.size()) {
            throw new NotFoundException(fileName + ": record " + id + " not found");
        }
        db.write(fileName, remaining);
    }

    protected String noMatchMessage(Map<String, Object> filters) {
        String rendered = filters.entrySet().stream()
                .map(filter -> "'" + filter.getKey() + "': '" + filter.getValue() + "'")
                .collect(Collectors.joining(", "));
        return fileName + ": no record matching {" + rendered + "}";
    }
}
