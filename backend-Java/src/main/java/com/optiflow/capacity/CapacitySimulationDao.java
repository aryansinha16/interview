package com.optiflow.capacity;

import com.optiflow.shared.BaseDao;
import com.optiflow.shared.JsonDb;
import org.springframework.stereotype.Repository;

@Repository
public class CapacitySimulationDao extends BaseDao {

    CapacitySimulationDao(JsonDb db) {
        super(db, "capacity_simulations_db.json");
    }
}
