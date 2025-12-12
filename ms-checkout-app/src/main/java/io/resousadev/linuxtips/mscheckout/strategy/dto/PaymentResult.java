package io.resousadev.linuxtips.mscheckout.strategy.dto;

/**
 * Resultado do processamento de um pagamento.
 * Record imutável contendo informações sobre o resultado da transação.
 *
 * @param success indica se o pagamento foi processado com sucesso
 * @param transactionId identificador único da transação
 * @param message mensagem descritiva do resultado
 * @param timestamp timestamp Unix em milissegundos do processamento
 */
public record PaymentResult(
        boolean success,
        String transactionId,
        String message,
        long timestamp
) {}
