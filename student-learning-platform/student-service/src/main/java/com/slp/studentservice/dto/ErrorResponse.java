package com.slp.studentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Standard error payload returned by all endpoints")
public class ErrorResponse {

    @Schema(example = "2025-01-15T10:15:30")
    private LocalDateTime timestamp;

    @Schema(example = "404")
    private int status;

    @Schema(example = "Not Found")
    private String error;

    @Schema(example = "Student not found with id: 99")
    private String message;

    @Schema(example = "/api/students/99")
    private String path;

    @Schema(description = "Field-level validation errors (only for 400 responses)",
            example = "{\"email\": \"Email must be valid\"}")
    private Map<String, String> validationErrors;
}
