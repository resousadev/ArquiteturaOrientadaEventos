package io.resousadev.linuxtips.mscheckout.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.resousadev.linuxtips.common.dto.ApiResponse;
import io.resousadev.linuxtips.mscheckout.dto.PaymentRequest;
import io.resousadev.linuxtips.mscheckout.dto.PaymentResponse;
import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.usecase.ProcessPaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for checkout operations including payment processing.
 * <p>
 * This controller exposes endpoints for:
 * <ul>
 *   <li>Processing payments using Strategy Pattern (multiple payment types)</li>
 *   <li>Finishing orders (legacy endpoint)</li>
 * </ul>
 * </p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>POST /v1/mscheckout/checkout/pay - Process payment with strategy selection</li>
 *   <li>POST /v1/mscheckout/orders - Finish order (legacy)</li>
 * </ul>
 *
 * @see ProcessPaymentUseCase
 * @see PaymentRequest
 * @see PaymentResponse
 * @since 1.0.0
 * @author Renan Sousa
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/mscheckout")
public class CheckoutController {

    private final EventBridgeProducer eventBridgeProducer;
    private final ProcessPaymentUseCase processPaymentUseCase;

    /**
     * Processes a payment using the Strategy Pattern.
     * <p>
     * This endpoint handles payment processing for multiple payment types:
     * <ul>
     *   <li>Credit Card: validates card and processes payment</li>
     *   <li>Debit Card: validates card with immediate debit</li>
     *   <li>PIX: generates QR code for instant payment</li>
     *   <li>Boleto: generates bank slip with barcode</li>
     * </ul>
     * </p>
     *
     * <h2>Request Example</h2>
     * <pre>{@code
     * POST /v1/mscheckout/checkout/pay
     * {
     *   "paymentType": "credit_card",
     *   "amount": "150.50",
     *   "origin": "web-checkout",
     *   "description": "Order #12345"
     * }
     * }</pre>
     *
     * <h2>Success Response (200 OK)</h2>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": null,
     *   "data": {
     *     "success": true,
     *     "transactionId": "CC-TXN-abc123",
     *     "message": "Credit card payment approved. Amount: R$ 150.50",
     *     "timestamp": 1702345678000,
     *     "paymentType": "credit_card"
     *   },
     *   "errorCode": null,
     *   "timestamp": "2025-12-11T10:30:00Z"
     * }
     * }</pre>
     *
     * <h2>Error Response (400 Bad Request)</h2>
     * <pre>{@code
     * {
     *   "success": false,
     *   "message": "Invalid payment type: invalid_type",
     *   "data": null,
     *   "errorCode": "VALIDATION_ERROR",
     *   "timestamp": "2025-12-11T10:30:00Z"
     * }
     * }</pre>
     *
     * @param request payment request with type, amount, and origin
     * @return ApiResponse containing PaymentResponse with transaction details
     */
    @PostMapping("/checkout/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody final PaymentRequest request) {
        
        log.info("Received payment request: type={}, amount={}, origin={}", 
                request.getPaymentType(), request.getAmount(), request.getOrigin());

        // Map DTO to domain model
        final Payment payment = new Payment(
            request.getOrigin(),
            request.getAmount(),
            "PENDING" // Initial status
        );

        // Execute payment processing use case
        final PaymentResult result = processPaymentUseCase.execute(payment, request.getPaymentType());

        // Map domain result to DTO response
        final PaymentResponse response = PaymentResponse.builder()
                .success(result.success())
                .transactionId(result.transactionId())
                .message(result.message())
                .timestamp(result.timestamp())
                .paymentType(request.getPaymentType().getValue())
                .build();

        log.info("Payment processed: success={}, transactionId={}, type={}", 
                result.success(), result.transactionId(), request.getPaymentType());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    /**
     * Finishes an order by publishing an event to EventBridge.
     * <p>
     * This is a legacy endpoint maintained for backward compatibility.
     * New integrations should use the {@code /checkout/pay} endpoint instead.
     * </p>
     *
     * @param payment payment data to publish as event
     * @deprecated Use {@link #processPayment(PaymentRequest)} instead
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    @PostMapping("/orders")
    public void finishOrder(@RequestBody final Payment payment) {
        log.info("Legacy endpoint called: /orders - origem={}, valor={}", 
                payment.origem(), payment.valor());
        eventBridgeProducer.finishOrder(payment);
    }

}
