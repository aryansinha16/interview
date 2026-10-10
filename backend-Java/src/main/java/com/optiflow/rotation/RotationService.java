package com.optiflow.rotation;

import com.optiflow.shared.JsonDb;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RotationService {

    private final RotationDao dao;

    RotationService(RotationDao dao) {
        this.dao = dao;
    }

    public static int findSurvivor(int poolSize, int rotationStep) {
        // TODO: implement by interviewee
        throw new UnsupportedOperationException("findSurvivor not implemented");
    }

    public RotationResponse create(RotationRequest request) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", JsonDb.newId("rot"));
        record.put("pool_size", request.poolSize());
        record.put("rotation_step", request.rotationStep());
        record.put("survivor_server_id", findSurvivor(request.poolSize(), request.rotationStep()));
        record.put("computed_at", JsonDb.nowIso());
        return toResponse(dao.insert(record));
    }

    public RotationResponse get(String rotationId) {
        return toResponse(dao.findById(rotationId));
    }

    public RotationResponse update(String rotationId, RotationRequest request) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("pool_size", request.poolSize());
        fields.put("rotation_step", request.rotationStep());
        fields.put("survivor_server_id", findSurvivor(request.poolSize(), request.rotationStep()));
        fields.put("computed_at", JsonDb.nowIso());
        return toResponse(dao.update(rotationId, fields));
    }

    public void delete(String rotationId) {
        dao.delete(rotationId);
    }

    private static RotationResponse toResponse(Map<String, Object> record) {
        // TODO: implement by interviewee
        throw new UnsupportedOperationException("toResponse not implemented");
    }
}
