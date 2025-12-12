package io.resousadev.linuxtips.mscheckout.dto;

import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;

/**
 * Response do processamento de pagamento.
 *
 * @param success indica se foi processado com sucesso
 * @param transactionId ID da transação
 * @param message mensagem descritiva
 * @param paymentType tipo de pagamento processado
 * @param timestamp timestamp do processamento
 */
public record PaymentResponse(
        boolean success,
        String transactionId,
        String message,
        PaymentTypeEnum paymentType,
        long timestamp
) {

    /**
     * Cria um PaymentResponse a partir de um PaymentResult.
     *
     * @param result resultado do processamento
     * @param paymentType tipo de pagamento
     * @return response formatado
     */
    public static PaymentResponse from(final PaymentResult result, final PaymentTypeEnum paymentType) {
        return new PaymentResponse(
                result.success(),
                result.transactionId(),
                result.message(),
                paymentType,
                result.timestamp()
        );
    }

}
