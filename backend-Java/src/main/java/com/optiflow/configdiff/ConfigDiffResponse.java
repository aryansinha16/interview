package com.optiflow.configdiff;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ConfigDiffResponse(
        @JsonProperty("config_id") String configId,
        @JsonProperty("source_env") String sourceEnv,
        @JsonProperty("target_env") String targetEnv,
        @JsonProperty("drift_score") Integer driftScore,
        @JsonProperty("diff_details") List<DiffDetail> diffDetails) {
}
