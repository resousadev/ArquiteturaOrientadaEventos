package io.resousadev.linuxtips.mscheckout.strategy.payment;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;

public interface PaymentStrategy {
    PaymentResult processPayment(Payment payment);
}
