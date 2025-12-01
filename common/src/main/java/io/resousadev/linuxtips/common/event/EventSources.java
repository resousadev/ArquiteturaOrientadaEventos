package io.resousadev.linuxtips.common.event;

/**
 * Constants for EventBridge event sources (microservice identifiers).
 */
public final class EventSources {

    private EventSources() {
        // Utility class - prevent instantiation
    }

    public static final String MS_CHECKOUT = "ms-checkout";
    public static final String MS_MANAGER_FILE = "manager-file";
}
