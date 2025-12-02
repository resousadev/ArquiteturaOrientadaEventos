package io.resousadev.linuxtips.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(final String resourceType, final String resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId), "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(final String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
