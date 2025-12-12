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
 * Concrete strategy implementation for processing Boleto (bank slip) payments.
 * <p>
 * Boleto is a Brazilian payment method where the system generates a bank slip
 * with a barcode that customers can pay at banks, lottery shops, or via internet banking.
 * </p>
 *
 * <h2>Processing Flow</h2>
 * <ol>
 *   <li>Validate payment amount</li>
 *   <li>Calculate due date (typically 3 business days)</li>
 *   <li>Generate barcode number (linha digitável)</li>
 *   <li>Create unique transaction ID</li>
 *   <li>Return success with barcode data</li>
 * </ol>
 *
 * <h2>Boleto Barcode</h2>
 * The generated barcode follows the FEBRABAN (Brazilian Banking Federation) standard:
 * <ul>
 *   <li>Bank code</li>
 *   <li>Currency code</li>
 *   <li>Due date factor</li>
 *   <li>Amount</li>
 *   <li>Verification digits</li>
 * </ul>
 *
 * <h2>Spring Bean Registration</h2>
 * Registered as {@code "boletoPaymentStrategy"} for auto-discovery.
 *
 * @see PaymentStrategy
 * @since 1.0.0
 * @author Renan Sousa
 */
@Slf4j
@Component("boletoPaymentStrategy")
public class BoletoPaymentStrategy implements PaymentStrategy {

    private static final String TRANSACTION_PREFIX = "BOLETO-";
    private static final int DUE_DATE_DAYS = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Processes a Boleto payment by generating a bank slip with barcode.
     * <p>
     * In a production environment, this would integrate with a banking service
     * provider (e.g., Banco do Brasil, Bradesco, Itaú) to generate valid boletos
     * and track payment status.
     * </p>
     *
     * @param payment payment data containing amount and metadata
     * @return {@link PaymentResult} with barcode generation outcome
     * @throws IllegalArgumentException if payment amount is invalid
     */
    @Override
    public PaymentResult processPayment(final Payment payment) {
        log.info("Processing Boleto payment: origem={}, valor={}", 
                payment.origem(), payment.valor());

        try {
            // Validate amount
            final BigDecimal amount = parseAndValidateAmount(payment.valor());

            // Generate transaction ID
            final String transactionId = generateTransactionId();

            // Calculate due date
            final LocalDate dueDate = LocalDate.now().plusDays(DUE_DATE_DAYS);

            // Generate barcode (linha digitável)
            final String barcode = generateBarcodeNumber(transactionId, amount, dueDate);

            final String message = String.format(
                "Boleto generated successfully. Amount: R$ %.2f. Due date: %s. Barcode: %s", 
                amount,
                dueDate.format(DATE_FORMATTER),
                barcode
            );

            log.info("Boleto payment processed successfully: transactionId={}, amount={}, dueDate={}", 
                    transactionId, amount, dueDate);

            return PaymentResult.success(transactionId, message);

        } catch (IllegalArgumentException e) {
            log.error("Boleto payment validation failed: {}", e.getMessage());
            return PaymentResult.failure("ERROR-" + UUID.randomUUID(), e.getMessage());
        } catch (Exception e) {
            log.error("Boleto payment processing failed: {}", e.getMessage(), e);
            return PaymentResult.failure(
                "ERROR-" + UUID.randomUUID(), 
                "Boleto processing error: " + e.getMessage()
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
     * Generates a unique transaction ID for Boleto payments.
     *
     * @return transaction ID with BOLETO prefix
     */
    private String generateTransactionId() {
        return TRANSACTION_PREFIX + UUID.randomUUID().toString();
    }

    /**
     * Generates a simplified Boleto barcode number (linha digitável).
     * <p>
     * In production, this should follow the FEBRABAN standard with proper
     * calculation of verification digits and integration with banking systems.
     * </p>
     * <p>
     * Format: BBBMC.CCCCC DDDDD.DDDDDD DDDDD.DDDDDD V FFFDDDDDDDDDD
     * Where:
     * <ul>
     *   <li>BBB = Bank code (3 digits)</li>
     *   <li>M = Currency code (1 digit, always 9 for Real)</li>
     *   <li>C = Verification digit</li>
     *   <li>D = Free field (document number, etc.)</li>
     *   <li>V = General verification digit</li>
     *   <li>FFF = Due date factor</li>
     * </ul>
     * </p>
     *
     * @param transactionId unique transaction reference
     * @param amount payment amount
     * @param dueDate payment due date
     * @return formatted barcode number
     */
    private String generateBarcodeNumber(final String transactionId, final BigDecimal amount, 
                                        final LocalDate dueDate) {
        // Simplified barcode generation (not production-ready)
        // In production: use proper FEBRABAN standard with modulo-10 and modulo-11 verification
        
        final String bankCode = "001"; // Banco do Brasil (example)
        final String currencyCode = "9"; // Real
        final long dueDateFactor = dueDate.toEpochDay();
        final String amountStr = String.format("%010d", amount.multiply(new BigDecimal("100")).longValue());
        
        // Generate a simplified barcode (not valid for production)
        final String barcodeBase = String.format(
            "%s%s%04d%s%s",
            bankCode,
            currencyCode,
            dueDateFactor % 10000,
            amountStr,
            transactionId.hashCode() & 0xFFFFFF
        );

        // Format with separators (FEBRABAN format)
        return formatBarcode(barcodeBase);
    }

    /**
     * Formats the barcode with proper separators according to FEBRABAN standard.
     *
     * @param barcodeBase raw barcode string
     * @return formatted barcode with dots and spaces
     */
    private String formatBarcode(final String barcodeBase) {
        // Simplified formatting
        // Production format: BBBMC.CCCCC DDDDD.DDDDDD DDDDD.DDDDDD V FFFDDDDDDDDDD
        if (barcodeBase.length() >= 47) {
            return String.format(
                "%s.%s %s.%s %s.%s %s %s",
                barcodeBase.substring(0, 5),
                barcodeBase.substring(5, 10),
                barcodeBase.substring(10, 15),
                barcodeBase.substring(15, 21),
                barcodeBase.substring(21, 26),
                barcodeBase.substring(26, 32),
                barcodeBase.substring(32, 33),
                barcodeBase.substring(33, Math.min(47, barcodeBase.length()))
            );
        }
        return barcodeBase;
    }

}
