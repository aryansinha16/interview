package com.optiflow.configdiff;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiffDetail(
        @JsonProperty("op") String op,
        @JsonProperty("line") String line) {
}
