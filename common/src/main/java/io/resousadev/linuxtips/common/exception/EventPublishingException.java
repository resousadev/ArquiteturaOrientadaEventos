package io.resousadev.linuxtips.common.exception;

/**
 * Exception thrown when event publishing fails.
 */
public class EventPublishingException extends BusinessException {

    public EventPublishingException(final String eventType, final Throwable cause) {
        super(String.format("Failed to publish event: %s", eventType), "EVENT_PUBLISHING_FAILED", cause);
    }

    public EventPublishingException(final String message) {
        super(message, "EVENT_PUBLISHING_FAILED");
    }
}
