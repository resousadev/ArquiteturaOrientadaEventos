package io.resousadev.linuxtips.managerfile.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.resousadev.linuxtips.common.event.BaseEvent;
import io.resousadev.linuxtips.common.event.EventTypes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

/**
 * SQS consumer for processing file-related events from other microservices.
 * 
 * <p>This consumer uses long-polling to efficiently receive messages from SQS.
 * It is only enabled when {@code aws.sqs.enabled=true} is set in configuration.</p>
 * 
 * @see <a href="https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html">SQS Polling</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true")
public class FileEventConsumer {

    private static final int MAX_MESSAGES_PER_POLL = 10;
    private static final int WAIT_TIME_SECONDS = 5;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.file-events-queue-url}")
    private String queueUrl;

    /**
     * Poll SQS queue for file-related events using long-polling.
     * 
     * <p>Long-polling reduces the number of empty responses and provides
     * cost savings over short-polling.</p>
     */
    @Scheduled(fixedDelayString = "${aws.sqs.poll-interval:5000}")
    public void pollMessages() {
        try {
            final ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(MAX_MESSAGES_PER_POLL)
                    .waitTimeSeconds(WAIT_TIME_SECONDS)
                    .attributeNames(QueueAttributeName.ALL)
                    .messageAttributeNames("All")
                    .build();

            final List<Message> messages = sqsClient.receiveMessage(request).messages();

            for (Message message : messages) {
                processMessage(message);
            }
        } catch (Exception e) {
            log.error("SQS polling failed: queueUrl={}, error={}", queueUrl, e.getMessage(), e);
        }
    }

    private void processMessage(final Message message) {
        try {
            log.debug("Processing SQS message: messageId={}", message.messageId());

            // Parse the event
            final BaseEvent<?> event = objectMapper.readValue(message.body(), BaseEvent.class);

            log.info("Event received: eventType={}, eventId={}, source={}", 
                    event.getEventType(), event.getEventId(), event.getSource());

            // Process based on event type
            handleEvent(event);

            // Delete message after successful processing
            deleteMessage(message);
        } catch (Exception e) {
            log.error("Message processing failed: messageId={}, error={}", message.messageId(), e.getMessage(), e);
        }
    }

    private void handleEvent(final BaseEvent<?> event) {
        // Route event to appropriate handler based on type using constants
        switch (event.getEventType()) {
            case EventTypes.CHECKOUT_COMPLETED -> handleCheckoutCompleted(event);
            case EventTypes.USER_CREATED -> handleUserCreated(event);
            default -> log.warn("Unknown event type received: eventType={}, eventId={}", 
                    event.getEventType(), event.getEventId());
        }
    }

    private void handleCheckoutCompleted(final BaseEvent<?> event) {
        log.info("Handling checkout completed: eventId={}", event.getEventId());
        // Implement checkout completed logic - e.g., generate invoice PDF
    }

    private void handleUserCreated(final BaseEvent<?> event) {
        log.info("Handling user created: eventId={}", event.getEventId());
        // Implement user created logic - e.g., create user folder
    }

    private void deleteMessage(final Message message) {
        final DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsClient.deleteMessage(deleteRequest);
        log.debug("SQS message deleted: messageId={}", message.messageId());
    }
}
