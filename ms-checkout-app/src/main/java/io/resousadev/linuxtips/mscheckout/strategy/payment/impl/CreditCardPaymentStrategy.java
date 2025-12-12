package io.resousadev.linuxtips.mscheckout.strategy.payment.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Concrete strategy implementation for processing credit card payments.
 * <p>
 * This strategy handles credit card payment processing including validation
 * of card data and amount verification.
 * </p>
 *
 * <h2>Processing Flow</h2>
 * <ol>
 *   <li>Validate payment amount is positive and numeric</li>
 *   <li>Simulate credit card validation (in production: call payment gateway)</li>
 *   <li>Generate unique transaction ID</li>
 *   <li>Return success result with transaction details</li>
 * </ol>
 *
 * <h2>Spring Bean Registration</h2>
 * Registered as {@code "creditCardPaymentStrategy"} for auto-discovery by
 * {@link io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategyFactory}.
 *
 * @see PaymentStrategy
 * @since 1.0.0
 * @author Renan Sousa
 */
@Slf4j
@Component("creditCardPaymentStrategy")
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final String TRANSACTION_PREFIX = "CC-TXN-";

    /**
     * Processes a credit card payment.
     * <p>
     * In a production environment, this would integrate with a payment gateway
     * (e.g., Stripe, PayPal, PagSeguro) to validate card details and process
     * the transaction.
     * </p>
     *
     * @param payment payment data containing amount and metadata
     * @return {@link PaymentResult} with transaction outcome
     * @throws IllegalArgumentException if payment amount is invalid
     */
    @Override
    public PaymentResult processPayment(final Payment payment) {
        log.info("Processing credit card payment: origem={}, valor={}", 
                payment.origem(), payment.valor());

        try {
            // Validate amount
            final BigDecimal amount = parseAndValidateAmount(payment.valor());

            // In production: integrate with payment gateway API
            // For now: simulate successful processing
            log.debug("Validating credit card for amount: {}", amount);
            
            final String transactionId = generateTransactionId();
            final String message = String.format(
                "Credit card payment approved. Amount: R$ %.2f", 
                amount
            );

            log.info("Credit card payment processed successfully: transactionId={}, amount={}", 
                    transactionId, amount);

            return PaymentResult.success(transactionId, message);

        } catch (IllegalArgumentException e) {
            log.error("Credit card payment validation failed: {}", e.getMessage());
            return PaymentResult.failure("ERROR-" + UUID.randomUUID(), e.getMessage());
        } catch (Exception e) {
            log.error("Credit card payment processing failed: {}", e.getMessage(), e);
            return PaymentResult.failure(
                "ERROR-" + UUID.randomUUID(), 
                "Credit card processing error: " + e.getMessage()
            );
        }
    }

    /**
     * Parses and validates the payment amount.
     *
     * @param valor the amount string to validate
     * @return parsed BigDecimal amount
     * @throws IllegalArgumentException if amount is invalid
     */
    private BigDecimal parseAndValidateAmount(final String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Payment amount cannot be null or empty");
        }

        try {
            final BigDecimal amount = new BigDecimal(valor);
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Payment amount must be positive");
            }

            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid payment amount format: " + valor);
        }
    }

    /**
     * Generates a unique transaction ID for credit card payments.
     *
     * @return transaction ID with CC-TXN prefix
     */
    private String generateTransactionId() {
        return TRANSACTION_PREFIX + UUID.randomUUID().toString();
    }

}
