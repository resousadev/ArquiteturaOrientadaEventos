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

    public void finishOrder(final Payment payment) {
        log.info("=== ENVIANDO EVENTO PARA EVENTBRIDGE ===");
        log.debug("Payment: {}", payment);
        log.debug("Event Bus: status-pedido-bus");
        log.debug("Region: us-east-1");

        PutEventsRequestEntry eventRequest = PutEventsRequestEntry.builder()
            .source(payment.origem())
            .detailType(payment.status())
            .detail("{ \"valor\": \"" + payment.valor() + "\" }")
            .eventBusName("status-pedido-bus")
            .build();

        try {
            PutEventsRequest request = PutEventsRequest.builder()
                    .entries(eventRequest)
                    .build();

            log.debug("Request construído: {}", request);

            PutEventsResponse response = eventBridgeClient.putEvents(request);

            log.debug("Response HTTP Status: {}", response.sdkHttpResponse().statusCode());
            log.debug("Failed Entry Count: {}", response.failedEntryCount());
            log.debug("Entries: {}", response.entries());

            if (response.failedEntryCount() > 0) {
                var failedEntry = response.entries().get(0);
                log.error("❌ FALHA AO ENVIAR EVENTO!");
                log.error("Status: {}", payment.status());
                log.error("ErrorCode: {}", failedEntry.errorCode());
                log.error("ErrorMessage: {}", failedEntry.errorMessage());
            } else {
                log.info("✅ EVENTO ENVIADO COM SUCESSO!");
                log.info("Status: {}", payment.status());
                log.info("EventId: {}", response.entries().get(0).eventId());
                log.info("HTTP Status: {}", response.sdkHttpResponse().statusCode());
            }
        } catch (Exception e) {
            log.error("❌ EXCEÇÃO ao enviar evento para EventBridge", e);
            log.error("Mensagem: {}", e.getMessage());
            log.error("Causa: {}", e.getCause());
            throw e;
        }

        log.info("=== FIM DO ENVIO ===");
    }
}
