package io.resousadev.linuxtips.mscheckout.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1/mscheckout")
public class CheckoutController {

    private final EventBridgeProducer eventBridgeProducer;

    @PostMapping("/orders")
    public void finishOrder(@RequestBody final Payment payment) {
        eventBridgeProducer.finishOrder(payment);
    }

}
