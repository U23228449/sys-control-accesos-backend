package com.estaciona.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Wrapper estándar para respuestas de error de la API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;
    private OffsetDateTime timestamp;
    private List<String> details;

    public ApiError() {
    }

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = OffsetDateTime.now();
    }

    public ApiError(int status, String error, String message, String path, List<String> details) {
        this(status, error, message, path);
        this.details = details;
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public List<String> getDetails() { return details; }
}
