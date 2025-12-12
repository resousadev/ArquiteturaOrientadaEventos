package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Estratégia para processamento de pagamentos PIX com geração de QR Code.
 */
@Component("PIX")
@Slf4j
public class PixPaymentStrategy implements PaymentStrategy {

    /**
     * Processa pagamento PIX gerando QR Code.
     *
     * @param payment dados do pagamento
     * @return resultado do processamento com QR Code
     */
    @Override
    public PaymentResult processPayment(final Payment payment) {
        log.info("Processando pagamento PIX origem={}", payment.origem());

        try {
            final BigDecimal amount = new BigDecimal(payment.valor());
            final String transactionId = "PIX-" + UUID.randomUUID();
            final String qrCode = generateQRCode(transactionId, amount);

            log.info("Pagamento PIX processado com sucesso. TransactionId={}, Valor={}",
                    transactionId, amount);

            return new PaymentResult(
                    true,
                    transactionId,
                    "Pagamento PIX processado com sucesso. QR Code: " + qrCode + ". Valor: R$ " + amount,
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
            log.error("Erro ao processar pagamento PIX", e);
            return new PaymentResult(
                    false,
                    "ERROR-" + UUID.randomUUID(),
                    "Erro ao processar pagamento: " + e.getMessage(),
                    System.currentTimeMillis()
            );
        }
    }

    private String generateQRCode(final String transactionId, final BigDecimal amount) {
        final String qrData = String.format("PIX|%s|%.2f", transactionId, amount);
        return Base64.getEncoder().encodeToString(qrData.getBytes());
    }

}
