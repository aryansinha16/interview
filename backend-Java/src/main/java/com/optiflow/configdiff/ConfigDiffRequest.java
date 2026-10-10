package com.optiflow.configdiff;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ConfigDiffRequest(
        @JsonProperty("config_id") @NotNull String configId,
        @JsonProperty("source_env") @NotNull String sourceEnv,
        @JsonProperty("target_env") @NotNull String targetEnv,
        @JsonProperty("source_config") @NotNull String sourceConfig,
        @JsonProperty("target_config") @NotNull String targetConfig) {
}
