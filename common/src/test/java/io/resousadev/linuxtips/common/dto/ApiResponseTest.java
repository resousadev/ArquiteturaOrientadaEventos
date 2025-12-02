package io.resousadev.linuxtips.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ApiResponse}.
 */
class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        // When
        ApiResponse<String> response = ApiResponse.success("test-data");

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertNull(response.getMessage());
        assertEquals("test-data", response.getData());
    }

    @Test
    void shouldCreateSuccessResponseWithMessage() {
        // When
        ApiResponse<String> response = ApiResponse.success("test-data", "Operation successful");

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertEquals("test-data", response.getData());
    }

    @Test
    void shouldCreateErrorResponse() {
        // When
        ApiResponse<String> response = ApiResponse.error("Something went wrong");

        // Then
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getData());
    }
}
