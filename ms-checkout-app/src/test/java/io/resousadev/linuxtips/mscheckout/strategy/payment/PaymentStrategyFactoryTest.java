package io.resousadev.linuxtips.mscheckout.strategy.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import io.resousadev.linuxtips.mscheckout.strategy.payment.impl.BoletoPaymentStrategy;
import io.resousadev.linuxtips.mscheckout.strategy.payment.impl.CreditCardPaymentStrategy;
import io.resousadev.linuxtips.mscheckout.strategy.payment.impl.PixPaymentStrategy;

@DisplayName("PaymentStrategyFactory Tests")
class PaymentStrategyFactoryTest {

    private PaymentStrategyFactory factory;
    private Map<String, PaymentStrategy> strategies;

    @BeforeEach
    void setUp() {
        // Simulate Spring's Map injection with concrete strategies
        strategies = Map.of(
            "creditCardPaymentStrategy", new CreditCardPaymentStrategy(),
            "pixPaymentStrategy", new PixPaymentStrategy(),
            "boletoPaymentStrategy", new BoletoPaymentStrategy()
        );
        
        factory = new PaymentStrategyFactory(strategies);
    }

    @Test
    @DisplayName("Should return CreditCardPaymentStrategy for CREDIT_CARD type")
    void shouldReturnCreditCardStrategy() {
        // Act
        final PaymentStrategy strategy = factory.getStrategy(PaymentTypeEnum.CREDIT_CARD);

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof CreditCardPaymentStrategy);
    }

    @Test
    @DisplayName("Should return PixPaymentStrategy for PIX type")
    void shouldReturnPixStrategy() {
        // Act
        final PaymentStrategy strategy = factory.getStrategy(PaymentTypeEnum.PIX);

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof PixPaymentStrategy);
    }

    @Test
    @DisplayName("Should return BoletoPaymentStrategy for BOLETO type")
    void shouldReturnBoletoStrategy() {
        // Act
        final PaymentStrategy strategy = factory.getStrategy(PaymentTypeEnum.BOLETO);

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof BoletoPaymentStrategy);
    }

    @Test
    @DisplayName("Should throw exception for null payment type")
    void shouldThrowExceptionForNullPaymentType() {
        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.getStrategy(null)
        );
        
        assertEquals("Payment type cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for unsupported payment type")
    void shouldThrowExceptionForUnsupportedPaymentType() {
        // Arrange - Create factory without debit card strategy
        final Map<String, PaymentStrategy> limitedStrategies = Map.of(
            "creditCardPaymentStrategy", new CreditCardPaymentStrategy()
        );
        final PaymentStrategyFactory limitedFactory = new PaymentStrategyFactory(limitedStrategies);

        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> limitedFactory.getStrategy(PaymentTypeEnum.DEBIT_CARD)
        );
        
        assertTrue(exception.getMessage().contains("not supported"));
        assertTrue(exception.getMessage().contains("debit_card"));
    }

    @Test
    @DisplayName("Should check if payment type is supported - CREDIT_CARD")
    void shouldCheckIfCreditCardIsSupported() {
        // Act
        final boolean supported = factory.isSupported(PaymentTypeEnum.CREDIT_CARD);

        // Assert
        assertTrue(supported);
    }

    @Test
    @DisplayName("Should check if payment type is supported - PIX")
    void shouldCheckIfPixIsSupported() {
        // Act
        final boolean supported = factory.isSupported(PaymentTypeEnum.PIX);

        // Assert
        assertTrue(supported);
    }

    @Test
    @DisplayName("Should check if payment type is supported - BOLETO")
    void shouldCheckIfBoletoIsSupported() {
        // Act
        final boolean supported = factory.isSupported(PaymentTypeEnum.BOLETO);

        // Assert
        assertTrue(supported);
    }

    @Test
    @DisplayName("Should return false for unsupported payment type")
    void shouldReturnFalseForUnsupportedPaymentType() {
        // Arrange - Create factory without debit card strategy
        final Map<String, PaymentStrategy> limitedStrategies = Map.of(
            "creditCardPaymentStrategy", new CreditCardPaymentStrategy()
        );
        final PaymentStrategyFactory limitedFactory = new PaymentStrategyFactory(limitedStrategies);

        // Act
        final boolean supported = limitedFactory.isSupported(PaymentTypeEnum.DEBIT_CARD);

        // Assert
        assertFalse(supported);
    }

    @Test
    @DisplayName("Should return false for null payment type in isSupported")
    void shouldReturnFalseForNullPaymentTypeInIsSupported() {
        // Act
        final boolean supported = factory.isSupported(null);

        // Assert
        assertFalse(supported);
    }

    @Test
    @DisplayName("Should initialize with correct number of strategies")
    void shouldInitializeWithCorrectNumberOfStrategies() {
        // The factory is already initialized in setUp with 3 strategies
        // This test verifies the initialization logic

        // Act - Create a new factory to test initialization
        final PaymentStrategyFactory newFactory = new PaymentStrategyFactory(strategies);

        // Assert - Factory should be able to retrieve all strategies
        assertNotNull(newFactory.getStrategy(PaymentTypeEnum.CREDIT_CARD));
        assertNotNull(newFactory.getStrategy(PaymentTypeEnum.PIX));
        assertNotNull(newFactory.getStrategy(PaymentTypeEnum.BOLETO));
    }

}
