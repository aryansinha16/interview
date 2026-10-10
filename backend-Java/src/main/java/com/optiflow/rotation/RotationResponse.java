package com.optiflow.rotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RotationResponse(
        @JsonProperty("id") String id,
        @JsonProperty("pool_size") Integer poolSize,
        @JsonProperty("rotation_step") Integer rotationStep,
        @JsonProperty("survivor_server_id") Integer survivorServerId,
        @JsonProperty("computed_at") String computedAt) {
}
