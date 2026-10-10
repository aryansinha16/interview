package com.optiflow.rotation;

import com.optiflow.shared.BaseDao;
import com.optiflow.shared.JsonDb;
import org.springframework.stereotype.Repository;

@Repository
public class RotationDao extends BaseDao {

    RotationDao(JsonDb db) {
        super(db, "server_rotation_db.json");
    }
}
