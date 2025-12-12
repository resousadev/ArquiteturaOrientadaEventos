package io.resousadev.linuxtips.mscheckout.exception;

public class InvalidPaymentTypeException extends RuntimeException {
    public InvalidPaymentTypeException(String message) {
        super(message);
    }
}
