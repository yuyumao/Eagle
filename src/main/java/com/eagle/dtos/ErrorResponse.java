package com.eagle.dtos;

public record ErrorResponse(
        int code,
        String message
) {}