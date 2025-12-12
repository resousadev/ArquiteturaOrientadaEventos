package io.resousadev.linuxtips.mscheckout.strategy.factory;

import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy getStrategy(PaymentTypeEnum paymentType) {
        PaymentStrategy strategy = strategies.get(paymentType.name());
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de pagamento não suportado: " + paymentType);
        }
        return strategy;
    }
}
