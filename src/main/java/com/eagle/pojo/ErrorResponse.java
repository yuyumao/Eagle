package com.eagle.pojo;

public record ErrorResponse(
        int code,
        String message
) {}