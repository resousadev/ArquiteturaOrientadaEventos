package io.resousadev.linuxtips.mscheckout.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.resousadev.linuxtips.mscheckout.model.Payment;
import lombok.extern.slf4j.Slf4j;
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
            .detailType(payment.status())
            .detail("{ \"valor\": \"" + payment.valor() + "\" }")
            .eventBusName("status-pedido-bus")
            .build();

        var response = eventBridgeClient().putEvents(r -> r.entries(eventRequest));
        
        if (response.failedEntryCount() > 0) {
            log.error("Falha ao enviar evento! Status do pagamento: {} | Erro: {}", 
                payment.status(), 
                response.entries().get(0).errorMessage());
        } else {
            log.info("Evento enviado com sucesso! Status do pagamento: {} | EventId: {} | HTTP Status: {}", 
                payment.status(),
                response.entries().get(0).eventId(),
                response.sdkHttpResponse().statusCode());
        }
    }
}
