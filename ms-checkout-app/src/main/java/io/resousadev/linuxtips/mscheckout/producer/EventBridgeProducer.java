package io.resousadev.linuxtips.mscheckout.producer;

import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventBridgeProducer {

    private final EventBridgeClient eventBridgeClient;

    private static final String EVENT_BUS_NAME = "status-pedido-bus";

    /**
     * Finaliza um pedido enviando evento para o AWS EventBridge.
     *
     * @param payment dados do pagamento a ser publicado como evento
     */
    public void finishOrder(final Payment payment) {
        log.debug("Publishing event to EventBridge: eventBus={}, source={}, detailType={}",
                EVENT_BUS_NAME, payment.origem(), payment.status());

        PutEventsRequestEntry eventRequest = PutEventsRequestEntry.builder()
            .source(payment.origem())
            .detailType(payment.status())
            .detail("{ \"valor\": \"" + payment.valor() + "\" }")
            .eventBusName(EVENT_BUS_NAME)
            .build();

        try {
            PutEventsRequest request = PutEventsRequest.builder()
                    .entries(eventRequest)
                    .build();

            PutEventsResponse response = eventBridgeClient.putEvents(request);

            if (response.failedEntryCount() > 0) {
                var failedEntry = response.entries().get(0);
                log.error("EventBridge publish failed: eventBus={}, detailType={}, errorCode={}, errorMessage={}",
                        EVENT_BUS_NAME, payment.status(), failedEntry.errorCode(), failedEntry.errorMessage());
            } else {
                log.info("Event published to EventBridge: eventId={}, eventBus={}, detailType={}",
                        response.entries().get(0).eventId(), EVENT_BUS_NAME, payment.status());
            }
        } catch (Exception e) {
            log.error("EventBridge publish exception: eventBus={}, detailType={}, error={}",
                    EVENT_BUS_NAME, payment.status(), e.getMessage(), e);
            throw e;
        }
    }
}
