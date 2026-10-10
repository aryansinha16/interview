package com.optiflow.rotation;

import com.jayway.jsonpath.JsonPath;
import com.optiflow.ApiContractTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RotationApiTest extends ApiContractTestBase {

    @Test
    void rotationLifecycleCreatesReadsRecomputesAndDeletes() throws Exception {
        String created = postJson("/api/v1/rotations", "{\"pool_size\":5,\"rotation_step\":2}", 201);
        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/api/v1/rotations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.pool_size").value(5))
                .andExpect(jsonPath("$.rotation_step").value(2))
                .andExpect(jsonPath("$.survivor_server_id").value(3))
                .andExpect(jsonPath("$.computed_at").isString())
                .andExpect(jsonPath("$.*", hasSize(5)));

        mockMvc.perform(put("/api/v1/rotations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pool_size\":7,\"rotation_step\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.pool_size").value(7))
                .andExpect(jsonPath("$.survivor_server_id").value(4));

        mockMvc.perform(delete("/api/v1/rotations/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/v1/rotations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("server_rotation_db.json: record " + id + " not found"));
    }

    @Test
    void missingRotationReturns404ForGetPutAndDelete() throws Exception {
        mockMvc.perform(get("/api/v1/rotations/rot_000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("record rot_000000 not found")));
        mockMvc.perform(put("/api/v1/rotations/rot_000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pool_size\":5,\"rotation_step\":2}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("record rot_000000 not found")));
        mockMvc.perform(delete("/api/v1/rotations/rot_000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("record rot_000000 not found")));
    }

    @Test
    void rotationValidationFailuresReturn422WithTheWireFieldName() throws Exception {
        mockMvc.perform(post("/api/v1/rotations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pool_size\":1,\"rotation_step\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("pool_size"));

        mockMvc.perform(post("/api/v1/rotations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pool_size\":5,\"rotation_step\":0}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details[0].field").value("rotation_step"));

        mockMvc.perform(post("/api/v1/rotations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pool_size\":\"5\",\"rotation_step\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("pool_size"));

        mockMvc.perform(post("/api/v1/rotations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rotation_step\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details[0].field").value("pool_size"));
    }

    @Test
    void unparseableJsonReturns422() throws Exception {
        mockMvc.perform(post("/api/v1/rotations").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasSize(1)));
    }
}
