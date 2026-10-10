package com.optiflow.capacity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CapacitySimulationRequest(
        @JsonProperty("customer_id") @NotNull String customerId,
        @JsonProperty("cost_deltas") @NotNull List<Integer> costDeltas,
        @JsonProperty("spend_budget") @NotNull @Min(0) Integer spendBudget,
        @JsonProperty("waiver_credits") @NotNull @Min(0) Integer waiverCredits) {
}
