package io.resousadev.linuxtips.mscheckout.strategy.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentResult Tests")
class PaymentResultTest {

    @Test
    @DisplayName("Should create successful payment result")
    void shouldCreateSuccessfulPaymentResult() {
        // Arrange
        final String transactionId = "TXN-123";
        final String message = "Payment approved";
        final long timestamp = System.currentTimeMillis();

        // Act
        final PaymentResult result = new PaymentResult(true, transactionId, message, timestamp);

        // Assert
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(transactionId, result.transactionId());
        assertEquals(message, result.message());
        assertEquals(timestamp, result.timestamp());
    }

    @Test
    @DisplayName("Should create failed payment result")
    void shouldCreateFailedPaymentResult() {
        // Arrange
        final String transactionId = "ERROR-456";
        final String message = "Payment declined";
        final long timestamp = System.currentTimeMillis();

        // Act
        final PaymentResult result = new PaymentResult(false, transactionId, message, timestamp);

        // Assert
        assertNotNull(result);
        assertFalse(result.success());
        assertEquals(transactionId, result.transactionId());
        assertEquals(message, result.message());
        assertEquals(timestamp, result.timestamp());
    }

    @Test
    @DisplayName("Should create success result using factory method")
    void shouldCreateSuccessResultUsingFactoryMethod() {
        // Arrange
        final String transactionId = "TXN-789";
        final String message = "Payment successful";

        // Act
        final PaymentResult result = PaymentResult.success(transactionId, message);

        // Assert
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(transactionId, result.transactionId());
        assertEquals(message, result.message());
        assertTrue(result.timestamp() > 0);
    }

    @Test
    @DisplayName("Should create failure result using factory method")
    void shouldCreateFailureResultUsingFactoryMethod() {
        // Arrange
        final String transactionId = "ERROR-999";
        final String message = "Invalid payment data";

        // Act
        final PaymentResult result = PaymentResult.failure(transactionId, message);

        // Assert
        assertNotNull(result);
        assertFalse(result.success());
        assertEquals(transactionId, result.transactionId());
        assertEquals(message, result.message());
        assertTrue(result.timestamp() > 0);
    }

    @Test
    @DisplayName("Should throw exception for null transaction ID")
    void shouldThrowExceptionForNullTransactionId() {
        // Arrange
        final long timestamp = System.currentTimeMillis();

        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentResult(true, null, "Message", timestamp)
        );
        
        assertEquals("Transaction ID cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank transaction ID")
    void shouldThrowExceptionForBlankTransactionId() {
        // Arrange
        final long timestamp = System.currentTimeMillis();

        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentResult(true, "   ", "Message", timestamp)
        );
        
        assertEquals("Transaction ID cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null message")
    void shouldThrowExceptionForNullMessage() {
        // Arrange
        final long timestamp = System.currentTimeMillis();

        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentResult(true, "TXN-123", null, timestamp)
        );
        
        assertEquals("Message cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank message")
    void shouldThrowExceptionForBlankMessage() {
        // Arrange
        final long timestamp = System.currentTimeMillis();

        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentResult(true, "TXN-123", "   ", timestamp)
        );
        
        assertEquals("Message cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero timestamp")
    void shouldThrowExceptionForZeroTimestamp() {
        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentResult(true, "TXN-123", "Message", 0)
        );
        
        assertEquals("Timestamp must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative timestamp")
    void shouldThrowExceptionForNegativeTimestamp() {
        // Act & Assert
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaymentResult(true, "TXN-123", "Message", -1)
        );
        
        assertEquals("Timestamp must be positive", exception.getMessage());
    }

}
