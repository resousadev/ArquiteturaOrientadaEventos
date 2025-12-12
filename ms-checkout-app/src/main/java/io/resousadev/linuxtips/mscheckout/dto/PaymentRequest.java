package io.resousadev.linuxtips.mscheckout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for payment processing requests.
 * <p>
 * This DTO encapsulates the necessary information to process a payment,
 * including the payment type, amount, and origin/source information.
 * </p>
 *
 * <h2>Validation</h2>
 * All fields are validated using Jakarta Bean Validation annotations:
 * <ul>
 *   <li>paymentType: must not be null</li>
 *   <li>amount: must not be null, blank, and greater than zero</li>
 *   <li>origin: must not be null or blank</li>
 * </ul>
 *
 * <h2>JSON Example</h2>
 * <pre>{@code
 * {
 *   "paymentType": "credit_card",
 *   "amount": "150.50",
 *   "origin": "checkout-api",
 *   "description": "Order #12345"
 * }
 * }</pre>
 *
 * @see PaymentTypeEnum
 * @since 1.0.0
 * @author Renan Sousa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * Type of payment method to be used.
     * Supported values: credit_card, debit_card, pix, boleto.
     */
    @NotNull(message = "Payment type is required")
    @JsonProperty("paymentType")
    private PaymentTypeEnum paymentType;

    /**
     * Payment amount as string to avoid floating-point precision issues.
     * Must be a positive decimal number (e.g., "100.00", "50.5").
     */
    @NotBlank(message = "Amount is required")
    @JsonProperty("amount")
    private String amount;

    /**
     * Origin or source of the payment request.
     * Used for tracking and auditing purposes.
     * Examples: "web-checkout", "mobile-app", "api-integration".
     */
    @NotBlank(message = "Origin is required")
    @JsonProperty("origin")
    private String origin;

    /**
     * Optional description or notes about the payment.
     * Can include order ID, customer reference, or other metadata.
     */
    @JsonProperty("description")
    private String description;

}
