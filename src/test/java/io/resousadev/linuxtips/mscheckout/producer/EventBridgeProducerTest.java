package io.resousadev.linuxtips.mscheckout.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import io.resousadev.linuxtips.mscheckout.model.Payment;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

/**
 * Unit tests for {@link EventBridgeProducer}.
 * Tests event publishing to AWS EventBridge with mocked client.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventBridgeProducer Unit Tests")
class EventBridgeProducerTest {

    @Mock
    private EventBridgeClient eventBridgeClient;

    @Mock
    private SdkHttpResponse sdkHttpResponse;

    @InjectMocks
    private EventBridgeProducer eventBridgeProducer;

    @Captor
    private ArgumentCaptor<PutEventsRequest> requestCaptor;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment("checkout-service", "150.00", "APPROVED");
    }

    @Test
    @DisplayName("Should send event successfully to EventBridge")
    void shouldSendEventSuccessfully() {
        // Given
        PutEventsResultEntry successEntry = PutEventsResultEntry.builder()
            .eventId("event-12345")
            .build();

        PutEventsResponse response = PutEventsResponse.builder()
            .failedEntryCount(0)
            .entries(List.of(successEntry))
            .build();

        when(sdkHttpResponse.statusCode()).thenReturn(200);
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
            .thenAnswer(invocation -> {
                // Attach SDK HTTP response
                return response.toBuilder()
                    .sdkHttpResponse(sdkHttpResponse)
                    .build();
            });

        // When
        eventBridgeProducer.finishOrder(payment);

        // Then
        verify(eventBridgeClient).putEvents(requestCaptor.capture());
        
        PutEventsRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.entries()).hasSize(1);
        
        var entry = capturedRequest.entries().get(0);
        assertThat(entry.source()).isEqualTo("checkout-service");
        assertThat(entry.detailType()).isEqualTo("APPROVED");
        assertThat(entry.detail()).contains("150.00");
        assertThat(entry.eventBusName()).isEqualTo("status-pedido-bus");
    }

    @Test
    @DisplayName("Should handle failed event entry")
    void shouldHandleFailedEventEntry() {
        // Given
        PutEventsResultEntry failedEntry = PutEventsResultEntry.builder()
            .errorCode("InternalError")
            .errorMessage("Event bus not found")
            .build();

        PutEventsResponse response = PutEventsResponse.builder()
            .failedEntryCount(1)
            .entries(List.of(failedEntry))
            .build();

        when(sdkHttpResponse.statusCode()).thenReturn(200);
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
            .thenAnswer(invocation -> response.toBuilder()
                .sdkHttpResponse(sdkHttpResponse)
                .build());

        // When - should not throw, just log error
        eventBridgeProducer.finishOrder(payment);

        // Then
        verify(eventBridgeClient).putEvents(any(PutEventsRequest.class));
    }

    @Test
    @DisplayName("Should rethrow exception when EventBridge client fails")
    void shouldRethrowExceptionWhenClientFails() {
        // Given
        RuntimeException awsException = new RuntimeException("AWS connection failed");
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
            .thenThrow(awsException);

        // When / Then
        assertThatThrownBy(() -> eventBridgeProducer.finishOrder(payment))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("AWS connection failed");
    }

    @Test
    @DisplayName("Should build correct event request structure")
    void shouldBuildCorrectEventRequestStructure() {
        // Given
        Payment pendingPayment = new Payment("payment-gateway", "99.99", "PENDING");
        
        PutEventsResultEntry successEntry = PutEventsResultEntry.builder()
            .eventId("event-pending-001")
            .build();

        PutEventsResponse response = PutEventsResponse.builder()
            .failedEntryCount(0)
            .entries(List.of(successEntry))
            .build();

        when(sdkHttpResponse.statusCode()).thenReturn(200);
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
            .thenAnswer(invocation -> response.toBuilder()
                .sdkHttpResponse(sdkHttpResponse)
                .build());

        // When
        eventBridgeProducer.finishOrder(pendingPayment);

        // Then
        verify(eventBridgeClient).putEvents(requestCaptor.capture());
        
        PutEventsRequest request = requestCaptor.getValue();
        assertThat(request.entries()).hasSize(1);
        
        var entry = request.entries().get(0);
        assertThat(entry.source()).isEqualTo("payment-gateway");
        assertThat(entry.detailType()).isEqualTo("PENDING");
        assertThat(entry.detail()).isEqualTo("{ \"valor\": \"99.99\" }");
        assertThat(entry.eventBusName()).isEqualTo("status-pedido-bus");
    }

    @Test
    @DisplayName("Should handle rejected payment status")
    void shouldHandleRejectedPaymentStatus() {
        // Given
        Payment rejectedPayment = new Payment("fraud-detection", "5000.00", "REJECTED");
        
        PutEventsResultEntry successEntry = PutEventsResultEntry.builder()
            .eventId("event-rejected-001")
            .build();

        PutEventsResponse response = PutEventsResponse.builder()
            .failedEntryCount(0)
            .entries(List.of(successEntry))
            .build();

        when(sdkHttpResponse.statusCode()).thenReturn(200);
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
            .thenAnswer(invocation -> response.toBuilder()
                .sdkHttpResponse(sdkHttpResponse)
                .build());

        // When
        eventBridgeProducer.finishOrder(rejectedPayment);

        // Then
        verify(eventBridgeClient).putEvents(requestCaptor.capture());
        
        var entry = requestCaptor.getValue().entries().get(0);
        assertThat(entry.detailType()).isEqualTo("REJECTED");
        assertThat(entry.detail()).contains("5000.00");
    }
}
