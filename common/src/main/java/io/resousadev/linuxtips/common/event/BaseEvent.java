package io.resousadev.linuxtips.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base event class for EventBridge communication between microservices.
 * All domain events should extend this class.
 *
 * @param <T> the type of the event payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent<T> {

    /**
     * Unique identifier for the event.
     */
    @JsonProperty("eventId")
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Type of the event (e.g., "checkout.created", "file.uploaded").
     */
    @JsonProperty("eventType")
    private String eventType;

    /**
     * Source microservice that generated the event.
     */
    @JsonProperty("source")
    private String source;

    /**
     * Timestamp when the event was created.
     */
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Version of the event schema.
     */
    @JsonProperty("version")
    @Builder.Default
    private String version = "1.0";

    /**
     * Correlation ID for tracing across services.
     */
    @JsonProperty("correlationId")
    private String correlationId;

    /**
     * The actual event payload/data.
     */
    @JsonProperty("payload")
    private T payload;

    /**
     * Optional metadata for additional context.
     */
    @JsonProperty("metadata")
    private Map<String, String> metadata;
}
