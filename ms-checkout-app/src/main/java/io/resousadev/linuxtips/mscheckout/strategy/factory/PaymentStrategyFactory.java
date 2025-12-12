package io.resousadev.linuxtips.mscheckout.strategy.factory;

import java.util.Map;

import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.exception.InvalidPaymentTypeException;
import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;

/**
 * Factory para seleção da estratégia de pagamento apropriada.
 * Utiliza auto-descoberta do Spring via Map injection.
 */
@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    /**
     * Obtém a estratégia de pagamento para o tipo especificado.
     *
     * @param paymentType tipo de pagamento
     * @return estratégia correspondente
     * @throws InvalidPaymentTypeException se o tipo não for suportado
     */
    public PaymentStrategy getStrategy(final PaymentTypeEnum paymentType) {
        final PaymentStrategy strategy = strategies.get(paymentType.name());
        if (strategy == null) {
            throw new InvalidPaymentTypeException("Tipo de pagamento não suportado: " + paymentType);
        }
        return strategy;
    }

}
