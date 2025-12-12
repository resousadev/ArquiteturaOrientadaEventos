package io.resousadev.linuxtips.mscheckout.usecase;

import org.springframework.stereotype.Service;

import io.resousadev.linuxtips.mscheckout.dto.PaymentRequest;
import io.resousadev.linuxtips.mscheckout.dto.PaymentResponse;
import io.resousadev.linuxtips.mscheckout.exception.InvalidPaymentTypeException;
import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.factory.PaymentStrategyFactory;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case para orquestração do processamento de pagamentos.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentStrategyFactory strategyFactory;
    private final EventBridgeProducer eventBridgeProducer;

    /**
     * Executa o fluxo completo de processamento de pagamento.
     *
     * @param request dados do pagamento
     * @return response com resultado do processamento
     * @throws InvalidPaymentTypeException se o tipo de pagamento for inválido
     */
    public PaymentResponse execute(final PaymentRequest request) {
        log.info("Executando processamento de pagamento tipo={}, origem={}",
                request.paymentType(), request.origem());

        try {
            final PaymentStrategy strategy = strategyFactory.getStrategy(request.paymentType());
            final Payment payment = new Payment(request.origem(), request.valor(), "PENDING");

            final PaymentResult result = strategy.processPayment(payment);

            if (result.success()) {
                final Payment processedPayment = new Payment(request.origem(), request.valor(), "PROCESSED");
                eventBridgeProducer.publishPaymentProcessedEvent(
                        processedPayment,
                        result.transactionId(),
                        request.paymentType()
                );
            }

            return PaymentResponse.from(result, request.paymentType());
        } catch (IllegalArgumentException e) {
            log.error("Tipo de pagamento inválido: {}", request.paymentType(), e);
            throw new InvalidPaymentTypeException("Tipo de pagamento inválido: " + request.paymentType());
        } catch (Exception e) {
            log.error("Erro ao processar pagamento", e);
            throw new RuntimeException("Erro ao processar pagamento: " + e.getMessage(), e);
        }
    }

}
