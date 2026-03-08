package com.interview.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Builder Pattern – wrapper response chuẩn cho toàn bộ API.
 * Dùng @Builder của Lombok để tạo object linh hoạt.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // không serialize field null
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;
    private String  errorCode;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ---- Static factory methods ----

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
