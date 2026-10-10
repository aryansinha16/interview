package com.optiflow.capacity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CapacitySimulationResponse(
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("cost_deltas") List<Integer> costDeltas,
        @JsonProperty("spend_budget") Integer spendBudget,
        @JsonProperty("waiver_credits") Integer waiverCredits,
        @JsonProperty("furthest_month") Integer furthestMonth) {
}
