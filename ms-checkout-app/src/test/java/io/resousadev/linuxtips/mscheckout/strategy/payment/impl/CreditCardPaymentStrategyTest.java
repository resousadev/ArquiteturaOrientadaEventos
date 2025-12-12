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

@DisplayName("CreditCardPaymentStrategy Tests")
class CreditCardPaymentStrategyTest {

    private CreditCardPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CreditCardPaymentStrategy();
    }

    @Test
    @DisplayName("Should process credit card payment successfully")
    void shouldProcessCreditCardPaymentSuccessfully() {
        // Arrange
        final Payment payment = new Payment("web-checkout", "100.50", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertNotNull(result);
        assertTrue(result.success(), "Payment should be successful");
        assertNotNull(result.transactionId());
        assertTrue(result.transactionId().startsWith("CC-TXN-"));
        assertTrue(result.message().contains("Credit card payment approved"));
        assertTrue(result.message().contains("R$"), "Message should contain R$ symbol");
        assertTrue(result.timestamp() > 0);
    }

    @Test
    @DisplayName("Should handle large payment amounts")
    void shouldHandleLargePaymentAmounts() {
        // Arrange
        final Payment payment = new Payment("web-checkout", "99999.99", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertTrue(result.success(), "Large payment should be successful");
        assertTrue(result.message().contains("R$"), "Message should contain amount");
    }

    @Test
    @DisplayName("Should reject null payment amount")
    void shouldRejectNullPaymentAmount() {
        // Arrange
        final Payment payment = new Payment("web-checkout", null, "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.transactionId().startsWith("ERROR-"));
        assertTrue(result.message().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject blank payment amount")
    void shouldRejectBlankPaymentAmount() {
        // Arrange
        final Payment payment = new Payment("web-checkout", "   ", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.message().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject zero payment amount")
    void shouldRejectZeroPaymentAmount() {
        // Arrange
        final Payment payment = new Payment("web-checkout", "0.00", "PENDING");

        // Act
        final PaymentResult result = strategy.processPayment(payment);

        // Assert
        assertFalse(result.success());
        assertTrue(result.message().contains("must be positive"));
    }

    @Test
    @DisplayName("Should reject negative payment amount")
    void shouldRejectNegativePaymentAmount() {
        // Arrange
        final Payment payment = new Payment("web-checkout", "-50.00", "PENDING");

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
        final Payment payment = new Payment("web-checkout", "abc", "PENDING");

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
        final Payment payment1 = new Payment("web-checkout", "100.00", "PENDING");
        final Payment payment2 = new Payment("web-checkout", "100.00", "PENDING");

        // Act
        final PaymentResult result1 = strategy.processPayment(payment1);
        final PaymentResult result2 = strategy.processPayment(payment2);

        // Assert
        assertNotNull(result1.transactionId());
        assertNotNull(result2.transactionId());
        assertFalse(result1.transactionId().equals(result2.transactionId()));
    }

}
