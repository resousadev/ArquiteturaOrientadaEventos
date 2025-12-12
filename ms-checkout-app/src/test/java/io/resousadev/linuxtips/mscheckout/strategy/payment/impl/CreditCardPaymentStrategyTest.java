package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

/**
 * Testes unitários para {@link CreditCardPaymentStrategy}.
 */
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
        final Payment payment = new Payment("test-service", "150.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("CC-");
        assertThat(result.message()).contains("cart");
        assertThat(result.timestamp()).isPositive();
    }

    @Test
    @DisplayName("Should process payment with negative amount")
    void shouldProcessPaymentWithNegativeAmount() {
        final Payment payment = new Payment("test-service", "-10.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("CC-");
    }

    @Test
    @DisplayName("Should process payment with zero amount")
    void shouldProcessPaymentWithZeroAmount() {
        final Payment payment = new Payment("test-service", "0.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("CC-");
    }

    @Test
    @DisplayName("Should process payment with large amount")
    void shouldProcessPaymentWithLargeAmount() {
        final Payment payment = new Payment("test-service", "99999.99", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("CC-");
    }

    @Test
    @DisplayName("Should handle invalid amount format")
    void shouldHandleInvalidAmountFormat() {
        final Payment payment = new Payment("test-service", "invalid", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).startsWith("ERROR-");
        assertThat(result.message()).contains("Formato de valor inválido");
    }

    @Test
    @DisplayName("Should handle null payment fields")
    void shouldHandleNullPaymentFields() {
        final Payment payment = new Payment(null, null, null);

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).startsWith("ERROR-");
    }
}
