package io.resousadev.linuxtips.common.event;

/**
 * Constants for EventBridge event types used across microservices.
 */
public final class EventTypes {

    private EventTypes() {
        // Utility class - prevent instantiation
    }

    // =============================================
    // Checkout Events
    // =============================================
    public static final String CHECKOUT_CREATED = "checkout.created";
    public static final String CHECKOUT_COMPLETED = "checkout.completed";
    public static final String CHECKOUT_CANCELLED = "checkout.cancelled";

    // =============================================
    // File Events
    // =============================================
    public static final String FILE_UPLOADED = "file.uploaded";
    public static final String FILE_DELETED = "file.deleted";
    public static final String FILE_PROCESSED = "file.processed";
    public static final String FILE_PROCESSING_FAILED = "file.processing.failed";

    // =============================================
    // User Events
    // =============================================
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";
}
