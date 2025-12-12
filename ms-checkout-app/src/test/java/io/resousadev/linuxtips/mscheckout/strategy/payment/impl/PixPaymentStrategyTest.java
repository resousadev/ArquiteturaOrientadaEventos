package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

@DisplayName("PixPaymentStrategy Tests")
class PixPaymentStrategyTest {

    private PixPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PixPaymentStrategy();
    }

    @Test
    @DisplayName("Should process PIX payment successfully")
    void shouldProcessPixPaymentSuccessfully() {
        // Arrange
        final Payment payment = new Payment("mobile-app", "50.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertNotNull(result);
        assertTrue(result.success(), "PIX payment should be successful");
        assertNotNull(result.transactionId());
        assertTrue(result.transactionId().startsWith("PIX-"));
        assertTrue(result.message().contains("PIX QR code generated successfully"));
        assertTrue(result.message().contains("R$"));
        assertTrue(result.message().contains("QR Code:"));
        assertTrue(result.timestamp() > 0);
    }

    @Test
    @DisplayName("Should generate QR code in message")
    void shouldGenerateQrCodeInMessage() {
        // Arrange
        final Payment payment = new Payment("mobile-app", "100.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertTrue(result.success());
        assertTrue(result.message().contains("QR Code:"));
        // QR code should be Base64 encoded
        final String[] parts = result.message().split("QR Code: ");
        assertTrue(parts.length > 1);
    }

    @Test
    @DisplayName("Should reject null payment amount")
    void shouldRejectNullPaymentAmount() {
        // Arrange
        final Payment payment = new Payment("mobile-app", null, "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.transactionId().startsWith("ERROR-"));
        assertTrue(result.message().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject negative payment amount")
    void shouldRejectNegativePaymentAmount() {
        // Arrange
        final Payment payment = new Payment("mobile-app", "-10.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.message().contains("must be positive"));
    }

    @Test
    @DisplayName("Should reject invalid amount format")
    void shouldRejectInvalidAmountFormat() {
        // Arrange
        final Payment payment = new Payment("mobile-app", "invalid", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.message().contains("Invalid payment amount format"));
    }

    @Test
    @DisplayName("Should generate unique transaction IDs")
    void shouldGenerateUniqueTransactionIds() {
        // Arrange
        final Payment payment1 = new Payment("mobile-app", "100.00", "PENDING");
        final Payment payment2 = new Payment("mobile-app", "100.00", "PENDING");

        // Act
        final PaymentResult result1 = strategy.processPayment(payment1);
        final PaymentResult result2 = strategy.processPayment(payment2);

        // Assert
        assertNotNull(result1.transactionId());
        assertNotNull(result2.transactionId());
        assertFalse(result1.transactionId().equals(result2.transactionId()));
    }

}
