package io.resousadev.linuxtips.mscheckout.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.resousadev.linuxtips.mscheckout.dto.PaymentRequest;
import io.resousadev.linuxtips.mscheckout.dto.PaymentResponse;
import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import io.resousadev.linuxtips.mscheckout.strategy.factory.PaymentStrategyFactory;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;

/**
 * Testes unitários para {@link ProcessPaymentUseCase}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCase Tests")
class ProcessPaymentUseCaseTest {

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private EventBridgeProducer eventBridgeProducer;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private ProcessPaymentUseCase useCase;

    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest("test-service", "100.00", PaymentTypeEnum.CREDIT_CARD);
    }

    @Test
    @DisplayName("Should process successful payment and publish event")
    void shouldProcessSuccessfulPaymentAndPublishEvent() {
        final PaymentResult result = new PaymentResult(
                true,
                "CC-123",
                "Payment processed",
                System.currentTimeMillis()
        );
        when(strategyFactory.getStrategy(PaymentTypeEnum.CREDIT_CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(Payment.class))).thenReturn(result);

        final PaymentResponse response = useCase.execute(request);

        assertThat(response.success()).isTrue();
        assertThat(response.transactionId()).isEqualTo("CC-123");
        assertThat(response.message()).isEqualTo("Payment processed");
        verify(eventBridgeProducer).publishPaymentProcessedEvent(
                any(Payment.class),
                eq("CC-123"),
                eq(PaymentTypeEnum.CREDIT_CARD)
        );
    }

    @Test
    @DisplayName("Should process failed payment and not publish event")
    void shouldProcessFailedPaymentAndNotPublishEvent() {
        final PaymentResult result = new PaymentResult(
                false,
                "ERROR-456",
                "Payment failed",
                System.currentTimeMillis()
        );
        when(strategyFactory.getStrategy(PaymentTypeEnum.PIX)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(Payment.class))).thenReturn(result);

        final PaymentRequest pixRequest = new PaymentRequest("test-service", "50.00", PaymentTypeEnum.PIX);
        final PaymentResponse response = useCase.execute(pixRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.transactionId()).isEqualTo("ERROR-456");
        assertThat(response.message()).isEqualTo("Payment failed");
        verify(eventBridgeProducer, never()).publishPaymentProcessedEvent(
                any(Payment.class),
                anyString(),
                any(PaymentTypeEnum.class)
        );
    }

    @Test
    @DisplayName("Should process BOLETO payment type")
    void shouldProcessBoletoPayment() {
        final PaymentResult result = new PaymentResult(
                true,
                "BOLETO-789",
                "Boleto generated",
                System.currentTimeMillis()
        );
        when(strategyFactory.getStrategy(PaymentTypeEnum.BOLETO)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(Payment.class))).thenReturn(result);

        final PaymentRequest boletoRequest = new PaymentRequest("test-service", "200.00", PaymentTypeEnum.BOLETO);
        final PaymentResponse response = useCase.execute(boletoRequest);

        assertThat(response.success()).isTrue();
        assertThat(response.paymentType()).isEqualTo(PaymentTypeEnum.BOLETO);
        verify(strategyFactory).getStrategy(PaymentTypeEnum.BOLETO);
    }

    @Test
    @DisplayName("Should convert string amount to BigDecimal correctly")
    void shouldConvertAmountCorrectly() {
        final PaymentResult result = new PaymentResult(
                true,
                "TX-001",
                "Success",
                System.currentTimeMillis()
        );
        when(strategyFactory.getStrategy(any(PaymentTypeEnum.class))).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(Payment.class))).thenReturn(result);

        final PaymentRequest decimalRequest = new PaymentRequest("test-service", "99.99", PaymentTypeEnum.CREDIT_CARD);
        useCase.execute(decimalRequest);

        verify(paymentStrategy).processPayment(any(Payment.class));
    }

    @Test
    @DisplayName("Should process all payment types")
    void shouldProcessAllPaymentTypes() {
        final PaymentResult result = new PaymentResult(true, "TX-OK", "Success", System.currentTimeMillis());
        when(strategyFactory.getStrategy(any(PaymentTypeEnum.class))).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(Payment.class))).thenReturn(result);

        useCase.execute(new PaymentRequest("test", "10.00", PaymentTypeEnum.CREDIT_CARD));
        useCase.execute(new PaymentRequest("test", "20.00", PaymentTypeEnum.PIX));
        useCase.execute(new PaymentRequest("test", "30.00", PaymentTypeEnum.BOLETO));
        useCase.execute(new PaymentRequest("test", "40.00", PaymentTypeEnum.DEBIT_CARD));

        verify(paymentStrategy, org.mockito.Mockito.times(4)).processPayment(any(Payment.class));
    }

    @Test
    @DisplayName("Should handle general exception")
    void shouldHandleGeneralException() {
        when(strategyFactory.getStrategy(any(PaymentTypeEnum.class)))
                .thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(Payment.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        final PaymentRequest request = new PaymentRequest("test", "10.00", PaymentTypeEnum.CREDIT_CARD);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao processar pagamento");
    }
}
