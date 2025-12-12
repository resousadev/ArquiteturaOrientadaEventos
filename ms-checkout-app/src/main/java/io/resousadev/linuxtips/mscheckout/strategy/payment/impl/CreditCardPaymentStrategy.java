package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Estratégia para processamento de pagamentos com cartão de crédito.
 */
@Component("CREDIT_CARD")
@Slf4j
public class CreditCardPaymentStrategy implements PaymentStrategy {

    /**
     * Processa pagamento com cartão de crédito.
     *
     * @param payment dados do pagamento
     * @return resultado do processamento
     */
    @Override
    public PaymentResult processPayment(final Payment payment) {
        log.info("Processando pagamento CREDIT_CARD origem={}", payment.origem());

        try {
            final BigDecimal amount = new BigDecimal(payment.valor());
            final String transactionId = "CC-" + UUID.randomUUID();

            log.info("Pagamento cartão processado com sucesso. TransactionId={}, Valor={}",
                    transactionId, amount);

            return new PaymentResult(
                    true,
                    transactionId,
                    "Pagamento com cartão de crédito processado com sucesso. Valor: R$ " + amount,
                    System.currentTimeMillis()
            );
        } catch (NumberFormatException e) {
            log.error("Formato de valor inválido: {}", payment.valor(), e);
            return new PaymentResult(
                    false,
                    "ERROR-" + UUID.randomUUID(),
                    "Formato de valor inválido",
                    System.currentTimeMillis()
            );
        } catch (Exception e) {
            log.error("Erro ao processar pagamento com cartão", e);
            return new PaymentResult(
                    false,
                    "ERROR-" + UUID.randomUUID(),
                    "Erro ao processar pagamento: " + e.getMessage(),
                    System.currentTimeMillis()
            );
        }
    }

}
