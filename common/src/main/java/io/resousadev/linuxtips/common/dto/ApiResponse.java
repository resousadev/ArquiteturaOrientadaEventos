package io.resousadev.linuxtips.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper for consistent REST responses across microservices.
 *
 * @param <T> the type of the response data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @JsonProperty("success")
    @NotNull
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("timestamp")
    @Builder.Default
    private java.time.Instant timestamp = java.time.Instant.now();

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(final T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with data and message.
     */
    public static <T> ApiResponse<T> success(final T data, final String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(final String message, final String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    /**
     * Creates an error response without error code.
     */
    public static <T> ApiResponse<T> error(final String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
