package io.resousadev.linuxtips.common.exception;

/**
 * Base exception for business rule violations across microservices.
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(final String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(final String message, final String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(final String message, final String errorCode, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
