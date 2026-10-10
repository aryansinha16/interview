package com.optiflow.shared.error;

import java.util.List;

public record ValidationErrorBody(String error, List<Detail> details) {

    public record Detail(String field, String message) {
    }
}
