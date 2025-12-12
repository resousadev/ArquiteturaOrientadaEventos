package io.resousadev.linuxtips.mscheckout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.resousadev.linuxtips.mscheckout.dto.PaymentRequest;
import io.resousadev.linuxtips.mscheckout.dto.PaymentResponse;
import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import io.resousadev.linuxtips.mscheckout.usecase.ProcessPaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller REST para operações de checkout.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/mscheckout")
public class CheckoutController {

    private final EventBridgeProducer eventBridgeProducer;
    private final ProcessPaymentUseCase processPaymentUseCase;

    /**
     * Finaliza um pedido (endpoint legado).
     *
     * @param payment dados do pagamento
     * @deprecated Use {@link #processPayment(PaymentRequest)} para novos fluxos
     */
    @Deprecated(since = "1.1", forRemoval = false)
    @PostMapping("/orders")
    public void finishOrder(@RequestBody final Payment payment) {
        eventBridgeProducer.finishOrder(payment);
    }

    /**
     * Processa um pagamento usando Strategy Pattern.
     *
     * @param request dados do pagamento
     * @return resposta com resultado do processamento
     */
    @PostMapping("/checkout/pay")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody final PaymentRequest request) {
        final PaymentResponse response = processPaymentUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

}
