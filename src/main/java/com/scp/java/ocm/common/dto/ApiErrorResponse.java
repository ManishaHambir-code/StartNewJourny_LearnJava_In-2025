package com.scp.java.ocm.common.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> fieldErrors;

    public ApiErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Collections.<String, String>emptyMap());
    }

    public ApiErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }
}
