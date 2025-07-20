package com.eagle.dtos;

import java.util.List;

public record BadRequestErrorResponse(
        int code,
        String message,
        List<FieldError> errors
) {
    public record FieldError(String field, String reason) {}
}