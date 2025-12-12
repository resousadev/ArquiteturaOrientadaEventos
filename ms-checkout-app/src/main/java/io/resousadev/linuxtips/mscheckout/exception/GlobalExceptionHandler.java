package io.resousadev.linuxtips.mscheckout.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.resousadev.linuxtips.common.dto.ApiResponse;

/**
 * Handler global para tratamento centralizado de exceções.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de tipo de pagamento inválido.
     *
     * @param ex exceção capturada
     * @return response com status 400
     */
    @ExceptionHandler(InvalidPaymentTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPaymentType(final InvalidPaymentTypeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_PAYMENT_TYPE"));
    }

    /**
     * Trata erros de validação de argumentos.
     *
     * @param ex exceção de validação
     * @return response com erros de validação e status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            final MethodArgumentNotValidException ex) {
        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Erro de validação")
                        .data(errors)
                        .errorCode("VALIDATION_ERROR")
                        .build());
    }

    /**
     * Trata exceções genéricas não tratadas.
     *
     * @param ex exceção capturada
     * @return response com status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(final Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro interno do servidor: " + ex.getMessage(),
                        "INTERNAL_SERVER_ERROR"));
    }

}
