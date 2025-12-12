package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

@DisplayName("BoletoPaymentStrategy Tests")
class BoletoPaymentStrategyTest {

    private BoletoPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BoletoPaymentStrategy();
    }

    @Test
    @DisplayName("Should process Boleto payment successfully")
    void shouldProcessBoletoPaymentSuccessfully() {
        // Arrange
        final Payment payment = new Payment("api-integration", "200.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertNotNull(result);
        assertTrue(result.success(), "Boleto payment should be successful");
        assertNotNull(result.transactionId());
        assertTrue(result.transactionId().startsWith("BOLETO-"));
        assertTrue(result.message().contains("Boleto generated successfully"));
        assertTrue(result.message().contains("R$"));
        assertTrue(result.message().contains("Due date:"));
        assertTrue(result.message().contains("Barcode:"));
        assertTrue(result.timestamp() > 0);
    }

    @Test
    @DisplayName("Should generate barcode in message")
    void shouldGenerateBarcodeInMessage() {
        // Arrange
        final Payment payment = new Payment("api-integration", "150.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertTrue(result.success());
        assertTrue(result.message().contains("Barcode:"));
        // Barcode should contain dots and spaces (FEBRABAN format)
        final String[] parts = result.message().split("Barcode: ");
        assertTrue(parts.length > 1);
    }

    @Test
    @DisplayName("Should include due date in message")
    void shouldIncludeDueDateInMessage() {
        // Arrange
        final Payment payment = new Payment("api-integration", "100.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertTrue(result.success());
        assertTrue(result.message().contains("Due date:"));
        // Date should be in format dd/MM/yyyy
        assertTrue(result.message().matches(".*Due date: \\d{2}/\\d{2}/\\d{4}.*"));
    }

    @Test
    @DisplayName("Should reject null payment amount")
    void shouldRejectNullPaymentAmount() {
        // Arrange
        final Payment payment = new Payment("api-integration", null, "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.transactionId().startsWith("ERROR-"));
        assertTrue(result.message().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject zero payment amount")
    void shouldRejectZeroPaymentAmount() {
        // Arrange
        final Payment payment = new Payment("api-integration", "0", "PENDING");

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
        final Payment payment = new Payment("api-integration", "not-a-number", "PENDING");

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
        final Payment payment1 = new Payment("api-integration", "100.00", "PENDING");
        final Payment payment2 = new Payment("api-integration", "100.00", "PENDING");

        // Act
        final PaymentResult result1 = strategy.processPayment(payment1);
        final PaymentResult result2 = strategy.processPayment(payment2);

        // Assert
        assertNotNull(result1.transactionId());
        assertNotNull(result2.transactionId());
        assertFalse(result1.transactionId().equals(result2.transactionId()));
    }

}
