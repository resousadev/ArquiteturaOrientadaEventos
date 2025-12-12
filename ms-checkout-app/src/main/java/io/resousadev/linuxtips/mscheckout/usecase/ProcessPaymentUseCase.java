package io.resousadev.linuxtips.mscheckout.usecase;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resousadev.linuxtips.mscheckout.exception.PaymentProcessingException;
import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import io.resousadev.linuxtips.mscheckout.strategy.dto.PaymentResult;
import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategy;
import io.resousadev.linuxtips.mscheckout.strategy.payment.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

/**
 * Use case for processing payments using the Strategy Pattern.
 * <p>
 * This service orchestrates the payment processing flow:
 * <ol>
 *   <li>Selects the appropriate payment strategy based on payment type</li>
 *   <li>Processes the payment using the selected strategy</li>
 *   <li>Publishes PAYMENT_PROCESSED event to EventBridge</li>
 * </ol>
 * </p>
 *
 * <h2>Architecture Layers</h2>
 * <ul>
 *   <li><b>Controller:</b> Receives HTTP requests and DTOs</li>
 *   <li><b>Use Case (this class):</b> Orchestrates business logic</li>
 *   <li><b>Strategy:</b> Implements payment-specific processing</li>
 *   <li><b>Producer:</b> Publishes events to EventBridge</li>
 * </ul>
 *
 * <h2>Event-Driven Integration</h2>
 * After successful or failed payment processing, this use case publishes
 * a {@code PAYMENT_PROCESSED} event to AWS EventBridge for downstream
 * consumers (e.g., order fulfillment, notifications).
 *
 * @see PaymentStrategyFactory
 * @see PaymentStrategy
 * @see EventBridgeProducer
 * @since 1.0.0
 * @author Renan Sousa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentStrategyFactory strategyFactory;
    private final EventBridgeProducer eventBridgeProducer;
    private final ObjectMapper objectMapper;

    private static final String EVENT_SOURCE = "ms-checkout.payment";
    private static final String EVENT_DETAIL_TYPE = "PAYMENT_PROCESSED";

    /**
     * Processes a payment using the appropriate strategy and publishes an event.
     * <p>
     * The payment processing flow:
     * <ol>
     *   <li>Validate payment type is supported</li>
     *   <li>Select payment strategy from factory</li>
     *   <li>Execute payment processing via strategy</li>
     *   <li>Publish PAYMENT_PROCESSED event to EventBridge</li>
     *   <li>Return payment result to caller</li>
     * </ol>
     * </p>
     *
     * @param payment the payment data
     * @param paymentType the type of payment to process
     * @return {@link PaymentResult} indicating processing outcome
     * @throws PaymentProcessingException if payment processing fails
     */
    public PaymentResult execute(final Payment payment, final PaymentTypeEnum paymentType) {
        log.info("Starting payment processing: type={}, origem={}, valor={}", 
                paymentType, payment.origem(), payment.valor());

        try {
            // Validate payment type
            if (!strategyFactory.isSupported(paymentType)) {
                throw new PaymentProcessingException(
                    "Payment type not supported: " + paymentType.getValue()
                );
            }

            // Select and execute payment strategy
            final PaymentStrategy strategy = strategyFactory.getStrategy(paymentType);
            final PaymentResult result = strategy.processPayment(payment);

            log.info("Payment processed: success={}, transactionId={}, type={}", 
                    result.success(), result.transactionId(), paymentType);

            // Publish event to EventBridge
            publishPaymentEvent(payment, paymentType, result);

            return result;

        } catch (IllegalArgumentException e) {
            log.error("Payment validation error: {}", e.getMessage());
            throw new PaymentProcessingException("Payment validation failed: " + e.getMessage(), e);
        } catch (PaymentProcessingException e) {
            // Re-throw payment processing exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected payment processing error: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes a PAYMENT_PROCESSED event to EventBridge.
     * <p>
     * Event structure:
     * <pre>{@code
     * {
     *   "source": "ms-checkout.payment",
     *   "detail-type": "PAYMENT_PROCESSED",
     *   "detail": {
     *     "paymentType": "credit_card",
     *     "success": true,
     *     "transactionId": "CC-TXN-xxx",
     *     "message": "Payment approved",
     *     "timestamp": 1702345678000,
     *     "origin": "checkout-api",
     *     "amount": "100.00"
     *   }
     * }
     * }</pre>
     * </p>
     *
     * @param payment original payment data
     * @param paymentType payment type used
     * @param result payment processing result
     */
    private void publishPaymentEvent(final Payment payment, final PaymentTypeEnum paymentType, 
                                     final PaymentResult result) {
        try {
            // Build event detail with payment result
            final PaymentEventDetail eventDetail = new PaymentEventDetail(
                paymentType.getValue(),
                result.success(),
                result.transactionId(),
                result.message(),
                result.timestamp(),
                payment.origem(),
                payment.valor()
            );

            final String detailJson = objectMapper.writeValueAsString(eventDetail);

            log.debug("Publishing PAYMENT_PROCESSED event: {}", detailJson);

            // Create Payment object for EventBridge (using existing model)
            final Payment eventPayment = new Payment(
                EVENT_SOURCE,
                payment.valor(),
                EVENT_DETAIL_TYPE
            );

            // Publish using existing EventBridge producer
            eventBridgeProducer.finishOrder(eventPayment);

            log.info("PAYMENT_PROCESSED event published: transactionId={}, paymentType={}", 
                    result.transactionId(), paymentType);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payment event: {}", e.getMessage(), e);
            // Don't fail the payment if event publishing fails
        } catch (Exception e) {
            log.error("Failed to publish payment event: {}", e.getMessage(), e);
            // Don't fail the payment if event publishing fails
        }
    }

    /**
     * Inner record for payment event detail structure.
     * <p>
     * This structure is serialized to JSON and sent as the event detail
     * to EventBridge consumers.
     * </p>
     */
    private record PaymentEventDetail(
        String paymentType,
        boolean success,
        String transactionId,
        String message,
        long timestamp,
        String origin,
        String amount
    ) {}

}
