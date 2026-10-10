package com.optiflow.configdiff;

import com.optiflow.shared.BaseDao;
import com.optiflow.shared.JsonDb;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigDiffDao extends BaseDao {

    ConfigDiffDao(JsonDb db) {
        super(db, "config_drift_logs_db.json");
    }
}
