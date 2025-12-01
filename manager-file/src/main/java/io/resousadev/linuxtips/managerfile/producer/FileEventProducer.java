package io.resousadev.linuxtips.managerfile.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.resousadev.linuxtips.common.dto.FileMetadataDto;
import io.resousadev.linuxtips.common.event.BaseEvent;
import io.resousadev.linuxtips.common.event.EventSources;
import io.resousadev.linuxtips.common.exception.EventPublishingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

/**
 * EventBridge producer for file-related events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventProducer {

    private final EventBridgeClient eventBridgeClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.eventbridge.bus-name:checkout-event-bus}")
    private String eventBusName;

    /**
     * Publish a file event to EventBridge.
     *
     * @param eventType the type of event
     * @param fileMetadata the file metadata payload
     */
    public void publishFileEvent(final String eventType, final FileMetadataDto fileMetadata) {
        try {
            final BaseEvent<FileMetadataDto> event = BaseEvent.<FileMetadataDto>builder()
                    .eventType(eventType)
                    .source(EventSources.MS_MANAGER_FILE)
                    .payload(fileMetadata)
                    .build();

            final String eventJson = objectMapper.writeValueAsString(event);

            final PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .eventBusName(eventBusName)
                    .source(EventSources.MS_MANAGER_FILE)
                    .detailType(eventType)
                    .detail(eventJson)
                    .build();

            final PutEventsRequest request = PutEventsRequest.builder()
                    .entries(entry)
                    .build();

            final var response = eventBridgeClient.putEvents(request);

            if (response.failedEntryCount() > 0) {
                log.error("Failed to publish event: eventType={}, failedCount={}", 
                        eventType, response.failedEntryCount());
                throw new EventPublishingException(eventType, null);
            }

            log.info("Event published successfully: eventType={}, eventId={}", 
                    eventType, event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: eventType={}", eventType, e);
            throw new EventPublishingException(eventType, e);
        }
    }
}
