package com.optiflow.capacity;

import com.optiflow.ApiContractTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CapacityApiTest extends ApiContractTestBase {

    @Test
    void capacitySimulationReturnsFurthestMonthAndItsHistory() throws Exception {
        postJson("/api/v1/capacity/simulate",
                "{\"customer_id\":\"cust_test_1\",\"cost_deltas\":[4,2,7,6,9,14,4,1],"
                        + "\"spend_budget\":10,\"waiver_credits\":2}", 201);

        mockMvc.perform(get("/api/v1/capacity/cust_test_1/simulations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customer_id").value("cust_test_1"))
                .andExpect(jsonPath("$[0].cost_deltas", hasSize(8)))
                .andExpect(jsonPath("$[0].spend_budget").value(10))
                .andExpect(jsonPath("$[0].waiver_credits").value(2))
                .andExpect(jsonPath("$[0].furthest_month").value(4))
                .andExpect(jsonPath("$[0].*", hasSize(5)));
    }

    @Test
    void capacityValidationFailuresReturn422() throws Exception {
        mockMvc.perform(post("/api/v1/capacity/simulate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_id\":\"cust_1\",\"cost_deltas\":[1],\"spend_budget\":-1,\"waiver_credits\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("spend_budget"));

        mockMvc.perform(post("/api/v1/capacity/simulate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_id\":\"cust_1\",\"cost_deltas\":[1],\"spend_budget\":10,\"waiver_credits\":\"x\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("waiver_credits"));

        mockMvc.perform(post("/api/v1/capacity/simulate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_id\":\"cust_1\",\"cost_deltas\":[\"x\"],\"spend_budget\":10,\"waiver_credits\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void capacityHistoryReturnsTheNewestSimulationFirst() throws Exception {
        postJson("/api/v1/capacity/simulate",
                "{\"customer_id\":\"cust_test_order\",\"cost_deltas\":[1,1],\"spend_budget\":10,\"waiver_credits\":1}", 201);
        postJson("/api/v1/capacity/simulate",
                "{\"customer_id\":\"cust_test_order\",\"cost_deltas\":[1,1],\"spend_budget\":11,\"waiver_credits\":2}", 201);

        mockMvc.perform(get("/api/v1/capacity/cust_test_order/simulations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].spend_budget").value(11))
                .andExpect(jsonPath("$[1].spend_budget").value(10));
    }
}
