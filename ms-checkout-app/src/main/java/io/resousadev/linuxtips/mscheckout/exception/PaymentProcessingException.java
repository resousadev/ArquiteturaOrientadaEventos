package io.resousadev.linuxtips.mscheckout.exception;

/**
 * Custom exception for payment processing errors.
 * <p>
 * This exception is thrown when a payment cannot be processed due to
 * validation failures, unsupported payment types, or processing errors.
 * </p>
 *
 * @since 1.0.0
 * @author Renan Sousa
 */
public class PaymentProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new payment processing exception with the specified detail message.
     *
     * @param message the detail message
     */
    public PaymentProcessingException(final String message) {
        super(message);
    }

    /**
     * Constructs a new payment processing exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public PaymentProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
