package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Estratégia para processamento de pagamentos via boleto bancário.
 */
@Component("BOLETO")
@Slf4j
public class BoletoPaymentStrategy implements PaymentStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int DUE_DATE_DAYS = 3;
    private static final int MAX_TRANSACTION_ID_LENGTH = 20;

    /**
     * Processa pagamento via boleto gerando código de barras.
     *
     * @param payment dados do pagamento
     * @return resultado do processamento com código de barras
     */
    @Override
    public PaymentResult processPayment(final Payment payment) {
        log.info("Processando pagamento BOLETO origem={}", payment.origem());

        try {
            final BigDecimal amount = new BigDecimal(payment.valor());
            final String transactionId = "BOLETO-" + UUID.randomUUID();
            final String barcode = generateBarcode(transactionId, amount);
            final LocalDate dueDate = LocalDate.now().plusDays(DUE_DATE_DAYS);

            log.info("Boleto gerado com sucesso. TransactionId={}, Valor={}, Vencimento={}",
                    transactionId, amount, dueDate.format(DATE_FORMATTER));

            return new PaymentResult(
                    true,
                    transactionId,
                    "Boleto gerado com sucesso. Código de barras: " + barcode
                            + ". Vencimento: " + dueDate.format(DATE_FORMATTER) + ". Valor: R$ " + amount,
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
            log.error("Erro ao processar boleto", e);
            return new PaymentResult(
                    false,
                    "ERROR-" + UUID.randomUUID(),
                    "Erro ao processar pagamento: " + e.getMessage(),
                    System.currentTimeMillis()
            );
        }
    }

    private String generateBarcode(final String transactionId, final BigDecimal amount) {
        final String sanitizedId = transactionId
                .substring(0, Math.min(MAX_TRANSACTION_ID_LENGTH, transactionId.length()))
                .replaceAll("[^0-9]", "");
        final long amountCents = amount.multiply(new BigDecimal("100")).longValue();
        return String.format("34191.09008 %s %010d", sanitizedId, amountCents);
    }

}
