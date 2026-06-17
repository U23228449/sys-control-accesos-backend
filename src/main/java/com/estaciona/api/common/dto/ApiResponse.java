package com.estaciona.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

/**
 * Wrapper genérico para respuestas exitosas de la API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private OffsetDateTime timestamp;

    private ApiResponse() {
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = true;
        resp.data = data;
        resp.timestamp = OffsetDateTime.now();
        return resp;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> resp = ok(data);
        resp.message = message;
        return resp;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public OffsetDateTime getTimestamp() { return timestamp; }
}
