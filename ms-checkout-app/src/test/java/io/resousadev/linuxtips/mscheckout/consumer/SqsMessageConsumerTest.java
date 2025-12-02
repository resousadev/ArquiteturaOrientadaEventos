package io.resousadev.linuxtips.mscheckout.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

/**
 * Unit tests for {@link SqsMessageConsumer}.
 * Tests SQS message polling, processing, and deletion with mocked client.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SqsMessageConsumer Unit Tests")
class SqsMessageConsumerTest {

    private static final String TEST_QUEUE_URL = "http://localhost:4566/000000000000/checkout-events-queue";
    private static final String TEST_MESSAGE_ID = "msg-12345";
    private static final String TEST_RECEIPT_HANDLE = "receipt-handle-abc123";

    @Mock
    private SqsClient sqsClient;

    @InjectMocks
    private SqsMessageConsumer sqsMessageConsumer;

    @Captor
    private ArgumentCaptor<ReceiveMessageRequest> receiveRequestCaptor;

    @Captor
    private ArgumentCaptor<DeleteMessageRequest> deleteRequestCaptor;

    @BeforeEach
    void setUp() {
        // Use Spring's ReflectionTestUtils - recommended approach for setting @Value fields in tests
        ReflectionTestUtils.setField(sqsMessageConsumer, "queueUrl", TEST_QUEUE_URL);
    }

    @Test
    @DisplayName("Should poll messages with long polling configuration")
    void shouldPollMessagesWithLongPolling() {
        // Given
        ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
            .messages(Collections.emptyList())
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(emptyResponse);

        // When
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient).receiveMessage(receiveRequestCaptor.capture());
        
        ReceiveMessageRequest capturedRequest = receiveRequestCaptor.getValue();
        assertThat(capturedRequest.queueUrl()).isEqualTo(TEST_QUEUE_URL);
        assertThat(capturedRequest.waitTimeSeconds()).isEqualTo(20); // Long polling
        assertThat(capturedRequest.maxNumberOfMessages()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should process and delete single message successfully")
    void shouldProcessAndDeleteSingleMessage() {
        // Given
        String messageBody = """
            {
                "version": "0",
                "id": "event-uuid",
                "detail-type": "APPROVED",
                "source": "ms-checkout",
                "detail": {"valor": "150.00"}
            }
            """;

        Message message = Message.builder()
            .messageId(TEST_MESSAGE_ID)
            .receiptHandle(TEST_RECEIPT_HANDLE)
            .body(messageBody)
            .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
            .messages(List.of(message))
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(response);

        // When
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient).deleteMessage(deleteRequestCaptor.capture());

        DeleteMessageRequest deleteRequest = deleteRequestCaptor.getValue();
        assertThat(deleteRequest.queueUrl()).isEqualTo(TEST_QUEUE_URL);
        assertThat(deleteRequest.receiptHandle()).isEqualTo(TEST_RECEIPT_HANDLE);
    }

    @Test
    @DisplayName("Should process and delete multiple messages")
    void shouldProcessAndDeleteMultipleMessages() {
        // Given
        Message message1 = Message.builder()
            .messageId("msg-001")
            .receiptHandle("receipt-001")
            .body("{\"detail-type\": \"APPROVED\"}")
            .build();

        Message message2 = Message.builder()
            .messageId("msg-002")
            .receiptHandle("receipt-002")
            .body("{\"detail-type\": \"PENDING\"}")
            .build();

        Message message3 = Message.builder()
            .messageId("msg-003")
            .receiptHandle("receipt-003")
            .body("{\"detail-type\": \"REJECTED\"}")
            .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
            .messages(List.of(message1, message2, message3))
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(response);

        // When
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient, times(3)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should not delete messages when queue is empty")
    void shouldNotDeleteMessagesWhenQueueIsEmpty() {
        // Given
        ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
            .messages(Collections.emptyList())
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(emptyResponse);

        // When
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should handle exception during polling gracefully")
    void shouldHandleExceptionDuringPollingGracefully() {
        // Given
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenThrow(new RuntimeException("SQS connection failed"));

        // When - should not throw, just log error
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should handle exception during message deletion gracefully")
    void shouldHandleExceptionDuringDeletionGracefully() {
        // Given
        Message message = Message.builder()
            .messageId(TEST_MESSAGE_ID)
            .receiptHandle(TEST_RECEIPT_HANDLE)
            .body("{\"detail\": \"test\"}")
            .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
            .messages(List.of(message))
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(response);
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
            .thenThrow(new RuntimeException("Delete failed"));

        // When - should not throw, just log error
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should process EventBridge formatted message")
    void shouldProcessEventBridgeFormattedMessage() {
        // Given - EventBridge wraps events in an envelope
        String eventBridgeMessage = """
            {
                "version": "0",
                "id": "12345678-1234-1234-1234-123456789012",
                "detail-type": "APPROVED",
                "source": "ms-checkout",
                "account": "000000000000",
                "time": "2025-11-30T10:00:00Z",
                "region": "us-east-1",
                "resources": [],
                "detail": {
                    "valor": "299.99"
                }
            }
            """;

        Message message = Message.builder()
            .messageId(TEST_MESSAGE_ID)
            .receiptHandle(TEST_RECEIPT_HANDLE)
            .body(eventBridgeMessage)
            .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
            .messages(List.of(message))
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(response);

        // When
        sqsMessageConsumer.pollMessages();

        // Then
        verify(sqsClient).deleteMessage(deleteRequestCaptor.capture());
        assertThat(deleteRequestCaptor.getValue().receiptHandle()).isEqualTo(TEST_RECEIPT_HANDLE);
    }
}
