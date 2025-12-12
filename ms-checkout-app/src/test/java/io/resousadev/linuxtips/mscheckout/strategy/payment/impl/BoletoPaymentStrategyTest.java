package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

/**
 * Testes unitários para {@link BoletoPaymentStrategy}.
 */
@DisplayName("BoletoPaymentStrategy Tests")
class BoletoPaymentStrategyTest {

    private BoletoPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BoletoPaymentStrategy();
    }

    @Test
    @DisplayName("Should process boleto payment successfully with barcode")
    void shouldProcessBoletoPaymentSuccessfully() {
        final Payment payment = new Payment("test-service", "250.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("BOLETO-");
        assertThat(result.message()).contains("Boleto");
        assertThat(result.message()).contains("Código de barras");
        assertThat(result.message()).contains("Vencimento");
        assertThat(result.timestamp()).isPositive();
    }

    @Test
    @DisplayName("Should generate valid barcode format")
    void shouldGenerateValidBarcodeFormat() {
        final Payment payment = new Payment("test-service", "100.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        final String message = result.message();
        assertThat(message).contains("34191.09008");
        assertThat(message).matches(".*\\d{5}\\.\\d{5} \\d+ \\d{10}.*");
    }

    @Test
    @DisplayName("Should include due date in message")
    void shouldIncludeDueDateInMessage() {
        final Payment payment = new Payment("test-service", "150.00", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.message()).contains("Vencimento:");
        assertThat(result.message()).matches(".*\\d{2}/\\d{2}/\\d{4}.*");
    }

    @Test
    @DisplayName("Should process payment with decimal amount")
    void shouldProcessPaymentWithDecimalAmount() {
        final Payment payment = new Payment("test-service", "99.99", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("BOLETO-");
    }

    @Test
    @DisplayName("Should process payment with integer amount")
    void shouldProcessPaymentWithIntegerAmount() {
        final Payment payment = new Payment("test-service", "1000", "APPROVED");

        final PaymentResult result = strategy.processPayment(payment);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("BOLETO-");
    }

    @Test
    @DisplayName("Should handle invalid amount format")
    void shouldHandleInvalidAmountFormat() {
        final Payment payment = new Payment("test-service", "not-a-number", "APPROVED");

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
