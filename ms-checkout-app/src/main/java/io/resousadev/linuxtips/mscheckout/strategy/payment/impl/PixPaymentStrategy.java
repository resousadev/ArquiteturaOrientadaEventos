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
 * Concrete strategy implementation for processing PIX payments.
 * <p>
 * PIX is the Brazilian Central Bank's instant payment system. This strategy
 * generates a QR code payload that customers can scan to complete the payment.
 * </p>
 *
 * <h2>Processing Flow</h2>
 * <ol>
 *   <li>Validate payment amount</li>
 *   <li>Generate PIX QR code payload (EMV format)</li>
 *   <li>Create unique transaction ID</li>
 *   <li>Return success with QR code data</li>
 * </ol>
 *
 * <h2>PIX QR Code</h2>
 * The generated QR code follows the EMV standard and includes:
 * <ul>
 *   <li>Merchant information</li>
 *   <li>Transaction amount</li>
 *   <li>Unique transaction reference</li>
 * </ul>
 *
 * <h2>Spring Bean Registration</h2>
 * Registered as {@code "pixPaymentStrategy"} for auto-discovery.
 *
 * @see PaymentStrategy
 * @since 1.0.0
 * @author Renan Sousa
 */
@Slf4j
@Component("pixPaymentStrategy")
public class PixPaymentStrategy implements PaymentStrategy {

    private static final String TRANSACTION_PREFIX = "PIX-";
    private static final String MERCHANT_NAME = "MS-Checkout";
    private static final String MERCHANT_CITY = "Sao Paulo";

    /**
     * Processes a PIX payment by generating a QR code payload.
     * <p>
     * In a production environment, this would integrate with the Brazilian
     * Central Bank's PIX platform or a payment service provider (PSP) to
     * generate a valid PIX QR code and track payment status.
     * </p>
     *
     * @param payment payment data containing amount and metadata
     * @return {@link PaymentResult} with QR code generation outcome
     * @throws IllegalArgumentException if payment amount is invalid
     */
    @Override
    public PaymentResult processPayment(final Payment payment) {
        log.info("Processing PIX payment: origem={}, valor={}", 
                payment.origem(), payment.valor());

        try {
            // Validate amount
            final BigDecimal amount = parseAndValidateAmount(payment.valor());

            // Generate transaction ID
            final String transactionId = generateTransactionId();

            // Generate PIX QR code payload (simplified EMV format)
            final String qrCodePayload = generatePixQrCode(transactionId, amount);

            final String message = String.format(
                "PIX QR code generated successfully. Amount: R$ %.2f. QR Code: %s", 
                amount, 
                qrCodePayload
            );

            log.info("PIX payment processed successfully: transactionId={}, amount={}", 
                    transactionId, amount);

            return PaymentResult.success(transactionId, message);

        } catch (IllegalArgumentException e) {
            log.error("PIX payment validation failed: {}", e.getMessage());
            return PaymentResult.failure("ERROR-" + UUID.randomUUID(), e.getMessage());
        } catch (Exception e) {
            log.error("PIX payment processing failed: {}", e.getMessage(), e);
            return PaymentResult.failure(
                "ERROR-" + UUID.randomUUID(), 
                "PIX processing error: " + e.getMessage()
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
     * Generates a unique transaction ID for PIX payments.
     *
     * @return transaction ID with PIX prefix
     */
    private String generateTransactionId() {
        return TRANSACTION_PREFIX + UUID.randomUUID().toString();
    }

    /**
     * Generates a simplified PIX QR code payload in EMV format.
     * <p>
     * In production, this should use the official PIX EMV specification
     * and integrate with a PSP or the Central Bank's platform.
     * </p>
     *
     * @param transactionId unique transaction reference
     * @param amount payment amount
     * @return Base64 encoded QR code payload
     */
    private String generatePixQrCode(final String transactionId, final BigDecimal amount) {
        // Simplified PIX QR code format (not production-ready)
        // In production: use proper EMV format with CRC16 checksum
        final String pixPayload = String.format(
            "PIX|MERCHANT:%s|CITY:%s|TXN:%s|AMOUNT:%.2f",
            MERCHANT_NAME,
            MERCHANT_CITY,
            transactionId,
            amount
        );

        // Encode to Base64 for easier transmission
        return Base64.getEncoder().encodeToString(pixPayload.getBytes());
    }

}
