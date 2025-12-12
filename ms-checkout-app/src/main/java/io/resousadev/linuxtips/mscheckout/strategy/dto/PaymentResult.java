package io.resousadev.linuxtips.mscheckout.strategy.dto;

public record PaymentResult(boolean success, String transactionId, String message, long timestamp) {}
