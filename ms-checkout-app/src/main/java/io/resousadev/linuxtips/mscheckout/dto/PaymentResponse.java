package io.resousadev.linuxtips.mscheckout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for payment processing responses.
 * <p>
 * This DTO encapsulates the result of a payment processing operation,
 * providing details about the transaction outcome, ID, and related information.
 * </p>
 *
 * <h2>Response Structure</h2>
 * The response includes:
 * <ul>
 *   <li>success: whether the payment was processed successfully</li>
 *   <li>transactionId: unique identifier for the transaction</li>
 *   <li>message: descriptive message about the transaction result</li>
 *   <li>timestamp: when the transaction was processed</li>
 *   <li>paymentType: type of payment method used</li>
 * </ul>
 *
 * <h2>JSON Example - Success</h2>
 * <pre>{@code
 * {
 *   "success": true,
 *   "transactionId": "CC-TXN-abc123",
 *   "message": "Credit card payment approved. Amount: R$ 150.50",
 *   "timestamp": 1702345678000,
 *   "paymentType": "credit_card"
 * }
 * }</pre>
 *
 * <h2>JSON Example - Failure</h2>
 * <pre>{@code
 * {
 *   "success": false,
 *   "transactionId": "ERROR-xyz789",
 *   "message": "Invalid payment amount format",
 *   "timestamp": 1702345678000,
 *   "paymentType": "credit_card"
 * }
 * }</pre>
 *
 * @since 1.0.0
 * @author Renan Sousa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * Indicates whether the payment was processed successfully.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Unique identifier for the transaction.
     * Format varies by payment type:
     * - Credit Card: "CC-TXN-{uuid}"
     * - PIX: "PIX-{uuid}"
     * - Boleto: "BOLETO-{uuid}"
     * - Error: "ERROR-{uuid}"
     */
    @JsonProperty("transactionId")
    private String transactionId;

    /**
     * Descriptive message about the transaction result.
     * Success examples:
     * - "Credit card payment approved. Amount: R$ 100.00"
     * - "PIX QR code generated successfully. Amount: R$ 50.00. QR Code: xxx"
     * - "Boleto generated successfully. Amount: R$ 200.00. Due date: 14/12/2025. Barcode: xxx"
     * 
     * Failure examples:
     * - "Payment amount must be positive"
     * - "Invalid payment amount format"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Unix timestamp in milliseconds when the transaction was processed.
     */
    @JsonProperty("timestamp")
    private long timestamp;

    /**
     * Type of payment method used for the transaction.
     * Values: "credit_card", "debit_card", "pix", "boleto"
     */
    @JsonProperty("paymentType")
    private String paymentType;

}
