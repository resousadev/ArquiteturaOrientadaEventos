package io.resousadev.linuxtips.common.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link BaseEvent}.
 */
class BaseEventTest {

    @Test
    void shouldCreateBaseEventWithPayload() {
        // Given
        String payload = "test-payload";
        String eventId = "event-123";
        Instant timestamp = Instant.now();

        // When
        BaseEvent<String> event = BaseEvent.<String>builder()
                .eventId(eventId)
                .timestamp(timestamp)
                .payload(payload)
                .build();

        // Then
        assertNotNull(event);
        assertEquals(eventId, event.getEventId());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(payload, event.getPayload());
    }

    @Test
    void shouldCreateBaseEventWithComplexPayload() {
        // Given
        record TestPayload(String name, int value) { }
        TestPayload payload = new TestPayload("test", 42);

        // When
        BaseEvent<TestPayload> event = BaseEvent.<TestPayload>builder()
                .eventId("event-456")
                .timestamp(Instant.now())
                .payload(payload)
                .build();

        // Then
        assertNotNull(event);
        assertEquals("test", event.getPayload().name());
        assertEquals(42, event.getPayload().value());
    }
}
