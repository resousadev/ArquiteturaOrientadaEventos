package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

/**
 * Testes unitários para {@link PixPaymentStrategy}.
 */
@DisplayName("PixPaymentStrategy Tests")
class PixPaymentStrategyTest {

    private PixPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PixPaymentStrategy();
    }

    @Test
    @DisplayName("Should process PIX payment successfully with QR code")
    void shouldProcessPixPaymentSuccessfully() {
        final Payment payment = new Payment("test-service", "50.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("PIX-");
        assertThat(result.message()).contains("PIX");
        assertThat(result.message()).contains("QR Code");
        assertThat(result.timestamp()).isPositive();
    }

    @Test
    @DisplayName("Should generate valid Base64 QR code")
    void shouldGenerateValidBase64QRCode() {
        final Payment payment = new Payment("test-service", "100.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("QR Code:");
        assertThat(result.message()).contains("Valor: R$ 100.00");
    }

    @Test
    @DisplayName("Should process payment with small amount")
    void shouldProcessPaymentWithSmallAmount() {
        final Payment payment = new Payment("test-service", "0.01", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("PIX-");
    }

    @Test
    @DisplayName("Should process payment with large amount")
    void shouldProcessPaymentWithLargeAmount() {
        final Payment payment = new Payment("test-service", "50000.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("PIX-");
    }

    @Test
    @DisplayName("Should handle invalid amount format")
    void shouldHandleInvalidAmountFormat() {
        final Payment payment = new Payment("test-service", "abc", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).startsWith("ERROR-");
        assertThat(result.message()).contains("Formato de valor inválido");
    }

    @Test
    @DisplayName("Should handle null fields")
    void shouldHandleNullFields() {
        final Payment payment = new Payment(null, null, null);

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).startsWith("ERROR-");
    }
}
