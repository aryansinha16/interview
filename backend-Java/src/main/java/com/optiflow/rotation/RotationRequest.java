package com.optiflow.rotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RotationRequest(
        @JsonProperty("pool_size") @NotNull @Min(2) Integer poolSize,
        @JsonProperty("rotation_step") @NotNull @Min(1) Integer rotationStep) {
}
