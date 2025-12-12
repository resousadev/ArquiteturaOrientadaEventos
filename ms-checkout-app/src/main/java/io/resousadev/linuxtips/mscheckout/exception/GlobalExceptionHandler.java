package io.resousadev.linuxtips.mscheckout.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.resousadev.linuxtips.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the checkout application.
 * <p>
 * This controller advice handles exceptions across the entire application
 * and returns consistent error responses using {@link ApiResponse}.
 * </p>
 *
 * @since 1.0.0
 * @author Renan Sousa
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles PaymentProcessingException thrown during payment processing.
     *
     * @param ex the payment processing exception
     * @return error response with 400 Bad Request status
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentProcessingException(
            final PaymentProcessingException ex) {
        log.error("Payment processing error: {}", ex.getMessage(), ex);

        final ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("PAYMENT_PROCESSING_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles IllegalArgumentException for validation errors.
     *
     * @param ex the illegal argument exception
     * @return error response with 400 Bad Request status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            final IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);

        final ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("VALIDATION_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex the method argument not valid exception
     * @return error response with 400 Bad Request status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            final MethodArgumentNotValidException ex) {
        
        final String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed");

        log.error("Request validation error: {}", errorMessage);

        final ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(errorMessage)
                .errorCode("VALIDATION_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles all other unhandled exceptions.
     *
     * @param ex the exception
     * @return error response with 500 Internal Server Error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(final Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        final ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
