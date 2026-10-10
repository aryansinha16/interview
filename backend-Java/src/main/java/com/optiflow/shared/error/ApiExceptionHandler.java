package com.optiflow.shared.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.optiflow.shared.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ValidationErrorBody> onValidationFailure(MethodArgumentNotValidException ex) {
        Class<?> dtoType = ex.getBindingResult().getTarget().getClass();
        List<ValidationErrorBody.Detail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorBody.Detail(jsonName(dtoType, error), error.getDefaultMessage()))
                .toList();
        return ResponseEntity.unprocessableEntity().body(new ValidationErrorBody("Validation failed", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ValidationErrorBody> onUnreadableBody(HttpMessageNotReadableException ex) {
        String field = ex.getCause() instanceof MismatchedInputException mismatched && !mismatched.getPath().isEmpty()
                ? mismatched.getPath().get(0).getFieldName()
                : "body";
        return ResponseEntity.unprocessableEntity().body(new ValidationErrorBody("Validation failed",
                List.of(new ValidationErrorBody.Detail(field, "Invalid value"))));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> onNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }

    private static String jsonName(Class<?> dtoType, FieldError error) {
        return Arrays.stream(dtoType.getRecordComponents())
                .filter(component -> component.getName().equals(error.getField()))
                .map(component -> component.getAccessor().getAnnotation(JsonProperty.class).value())
                .findFirst()
                .orElse(error.getField());
    }
}
