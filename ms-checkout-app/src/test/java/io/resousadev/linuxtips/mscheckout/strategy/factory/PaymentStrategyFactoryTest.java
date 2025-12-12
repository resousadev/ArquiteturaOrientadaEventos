package io.resousadev.linuxtips.mscheckout.strategy.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.exception.InvalidPaymentTypeException;
import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import io.resousadev.linuxtips.mscheckout.strategy.payment.impl.BoletoPaymentStrategy;
import io.resousadev.linuxtips.mscheckout.strategy.payment.impl.CreditCardPaymentStrategy;
import io.resousadev.linuxtips.mscheckout.strategy.payment.impl.PixPaymentStrategy;

/**
 * Testes unitários para {@link PaymentStrategyFactory}.
 */
@DisplayName("PaymentStrategyFactory Tests")
class PaymentStrategyFactoryTest {

    private PaymentStrategyFactory factory;
    private Map<String, PaymentStrategy> strategies;

    @BeforeEach
    void setUp() {
        strategies = new HashMap<>();
        strategies.put("CREDIT_CARD", new CreditCardPaymentStrategy());
        strategies.put("PIX", new PixPaymentStrategy());
        strategies.put("BOLETO", new BoletoPaymentStrategy());
        factory = new PaymentStrategyFactory(strategies);
    }

    @Test
    @DisplayName("Should return CreditCardPaymentStrategy for CREDIT_CARD type")
    void shouldReturnCreditCardStrategy() {
        final PaymentStrategy strategy = factory.getStrategy(PaymentTypeEnum.CREDIT_CARD);

        assertThat(strategy).isInstanceOf(CreditCardPaymentStrategy.class);
    }

    @Test
    @DisplayName("Should return PixPaymentStrategy for PIX type")
    void shouldReturnPixStrategy() {
        final PaymentStrategy strategy = factory.getStrategy(PaymentTypeEnum.PIX);

        assertThat(strategy).isInstanceOf(PixPaymentStrategy.class);
    }

    @Test
    @DisplayName("Should return BoletoPaymentStrategy for BOLETO type")
    void shouldReturnBoletoStrategy() {
        final PaymentStrategy strategy = factory.getStrategy(PaymentTypeEnum.BOLETO);

        assertThat(strategy).isInstanceOf(BoletoPaymentStrategy.class);
    }

    @Test
    @DisplayName("Should throw InvalidPaymentTypeException when strategy not found")
    void shouldThrowExceptionWhenStrategyNotFound() {
        final Map<String, PaymentStrategy> emptyStrategies = new HashMap<>();
        final PaymentStrategyFactory emptyFactory = new PaymentStrategyFactory(emptyStrategies);

        assertThatThrownBy(() -> emptyFactory.getStrategy(PaymentTypeEnum.CREDIT_CARD))
                .isInstanceOf(InvalidPaymentTypeException.class)
                .hasMessageContaining("não suportado")
                .hasMessageContaining("CREDIT_CARD");
    }

    @Test
    @DisplayName("Should throw InvalidPaymentTypeException for DEBIT_CARD type")
    void shouldThrowExceptionForDebitCard() {
        assertThatThrownBy(() -> factory.getStrategy(PaymentTypeEnum.DEBIT_CARD))
                .isInstanceOf(InvalidPaymentTypeException.class)
                .hasMessageContaining("DEBIT_CARD");
    }
}
