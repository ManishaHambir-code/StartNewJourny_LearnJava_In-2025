package com.scp.java.ocm.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import javax.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.scp.java.ocm.common.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> duplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> rule(BusinessRuleViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        for (FieldError field : ex.getBindingResult().getFieldErrors()) {
            errors.put(field.getField(), field.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ApiErrorResponse(400, "Bad Request",
                "Validation failed for request payload", req.getRequestURI(), errors));
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class })
    public ResponseEntity<ApiErrorResponse> malformed(Exception ex, HttpServletRequest req) {
        String message = "Request body is missing or malformed";
        if (ex instanceof MethodArgumentTypeMismatchException) {
            message = "Invalid value for parameter '" + ((MethodArgumentTypeMismatchException) ex).getName() + "'";
        } else if (ex instanceof MissingServletRequestParameterException) {
            message = "Missing required parameter '"
                    + ((MissingServletRequestParameterException) ex).getParameterName() + "'";
        }
        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> method(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Request method '" + ex.getMethod() + "' is not supported", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", req);
    }

    @ExceptionHandler({ ConstraintViolationException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiErrorResponse> invalidParameter(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception ex, HttpServletRequest req) {
        LOGGER.error("Unhandled exception while processing {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }
}
