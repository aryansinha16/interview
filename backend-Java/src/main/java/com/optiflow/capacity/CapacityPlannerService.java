package com.optiflow.capacity;

import com.optiflow.shared.JsonDb;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CapacityPlannerService {

    private final CapacitySimulationDao dao;

    CapacityPlannerService(CapacitySimulationDao dao) {
        this.dao = dao;
    }

    public static int furthestMonth(List<Integer> costDeltas, int spendBudget, int waiverCredits) {
        // TODO: implement by interviewee
        throw new UnsupportedOperationException("furthestMonth not implemented");
    }

    public CapacitySimulationResponse simulate(CapacitySimulationRequest request) {
        int furthestMonth = furthestMonth(request.costDeltas(), request.spendBudget(), request.waiverCredits());

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", JsonDb.newId("sim"));
        record.put("customer_id", request.customerId());
        record.put("cost_deltas", request.costDeltas());
        record.put("spend_budget", request.spendBudget());
        record.put("waiver_credits", request.waiverCredits());
        record.put("furthest_month", furthestMonth);
        dao.insert(record);

        return new CapacitySimulationResponse(request.customerId(), request.costDeltas(),
                request.spendBudget(), request.waiverCredits(), furthestMonth);
    }

    public List<CapacitySimulationResponse> history(String customerId) {
        return dao.findAll(Map.of("customer_id", customerId)).reversed().stream()
                .map(CapacityPlannerService::toResponse)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private static CapacitySimulationResponse toResponse(Map<String, Object> record) {
        // TODO: implement by interviewee
        throw new UnsupportedOperationException("toResponse not implemented");
    }
}
