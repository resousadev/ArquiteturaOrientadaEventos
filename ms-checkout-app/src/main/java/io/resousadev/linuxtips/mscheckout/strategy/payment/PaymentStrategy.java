package io.resousadev.linuxtips.mscheckout.strategy.payment;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

/**
 * Strategy interface para processamento de diferentes tipos de pagamento.
 * Implementa o padrão Strategy permitindo adicionar novos métodos de pagamento
 * sem modificar código existente (Open/Closed Principle).
 */
public interface PaymentStrategy {

    /**
     * Processa um pagamento usando a estratégia específica.
     *
     * @param payment dados do pagamento a ser processado
     * @return resultado do processamento contendo sucesso, ID transação, mensagem e timestamp
     */
    PaymentResult processPayment(Payment payment);

}
