package com.optiflow.configdiff;

import com.optiflow.ApiContractTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConfigDiffApiTest extends ApiContractTestBase {

    @Test
    void configDiffReturnsDriftScoreAndDetailsAndItsHistory() throws Exception {
        String body = "{\"config_id\":\"cfg_test_1\",\"source_env\":\"staging\",\"target_env\":\"production\","
                + "\"source_config\":\"server_port=8080\\ntimeout=30\","
                + "\"target_config\":\"server_port=9090\\ntimeout=60\\nssl=true\"}";

        String created = postJson("/api/v1/configs/diff", body, 201);
        if (!created.contains("\"drift_score\":12")) {
            throw new AssertionError("unexpected body: " + created);
        }

        mockMvc.perform(get("/api/v1/configs/cfg_test_1/diffs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].config_id").value("cfg_test_1"))
                .andExpect(jsonPath("$[0].source_env").value("staging"))
                .andExpect(jsonPath("$[0].target_env").value("production"))
                .andExpect(jsonPath("$[0].drift_score").value(12))
                .andExpect(jsonPath("$[0].diff_details", hasSize(1)))
                .andExpect(jsonPath("$[0].diff_details[0].op").value("replace"))
                .andExpect(jsonPath("$[0].diff_details[0].line").value("server_port"))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].*", hasSize(5)));
    }

    @Test
    void configDiffValidationFailuresReturn422() throws Exception {
        mockMvc.perform(post("/api/v1/configs/diff").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"config_id\":\"cfg_1\",\"source_env\":\"staging\",\"target_env\":\"production\","
                                + "\"target_config\":\"a=1\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("source_config"));
    }

    @Test
    void numericTargetConfigIsRejectedWith422() throws Exception {
        mockMvc.perform(post("/api/v1/configs/diff").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"config_id\":\"c\",\"source_env\":\"a\",\"target_env\":\"b\","
                                + "\"source_config\":\"x\",\"target_config\":5}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("target_config"));
    }

    @Test
    void emptyConfigStringsAreStillAccepted() throws Exception {
        mockMvc.perform(post("/api/v1/configs/diff").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"config_id\":\"\",\"source_env\":\"\",\"target_env\":\"\","
                                + "\"source_config\":\"\",\"target_config\":\"\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.config_id").value(""))
                .andExpect(jsonPath("$.drift_score").value(0))
                .andExpect(jsonPath("$.diff_details", hasSize(0)));
    }

    @Test
    void configDiffHistoryReturnsTheNewestRecordFirst() throws Exception {
        postJson("/api/v1/configs/diff",
                "{\"config_id\":\"cfg_test_order\",\"source_env\":\"first\",\"target_env\":\"production\","
                        + "\"source_config\":\"a=1\",\"target_config\":\"a=2\"}", 201);
        postJson("/api/v1/configs/diff",
                "{\"config_id\":\"cfg_test_order\",\"source_env\":\"second\",\"target_env\":\"production\","
                        + "\"source_config\":\"b=1\",\"target_config\":\"b=2\"}", 201);

        mockMvc.perform(get("/api/v1/configs/cfg_test_order/diffs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].source_env").value("second"))
                .andExpect(jsonPath("$[1].source_env").value("first"));
    }
}
