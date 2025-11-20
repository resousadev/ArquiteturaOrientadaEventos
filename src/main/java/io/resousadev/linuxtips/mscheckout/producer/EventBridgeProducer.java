package io.resousadev.linuxtips.mscheckout.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;


@Component
@Slf4j
public class EventBridgeProducer {
    
    @Bean
    private EventBridgeClient eventBridgeClient() {
        return EventBridgeClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build();
    }

    public void finishOrder(Payment payment) {
        log.info("Enviando evento para o Event Bridge...");
        PutEventsRequestEntry eventRequest = PutEventsRequestEntry.builder()
            .source("ms-checkout")
            .detailType(payment.status)
            .detail("{ \"valor\": \"" + payment.valor + "\" }")
            .eventBusName("status-pedido-bus")
            .build();

        eventBridgeClient().putEvents(r -> {
            log.info("Evento {} enviado com sucesso!", r);
            r.entries(eventRequest);
        });
    }
}
