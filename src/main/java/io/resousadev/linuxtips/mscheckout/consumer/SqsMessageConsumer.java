package io.resousadev.linuxtips.mscheckout.consumer;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

/**
 * Consumer for processing messages from the SQS checkout-events-queue.
 * 
 * <p>This consumer uses long polling (20 seconds) for efficient message retrieval.
 * Messages are received from EventBridge via SQS and currently just logged.
 * Business logic will be implemented in a future iteration.</p>
 * 
 * <p>Message flow:
 * <pre>
 * EventBridge (status-pedido-bus) → SQS (checkout-events-queue) → SqsMessageConsumer
 * </pre>
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SqsMessageConsumer {

    private static final int LONG_POLLING_WAIT_TIME_SECONDS = 20;
    private static final int MAX_NUMBER_OF_MESSAGES = 10;

    private final SqsClient sqsClient;

    @Value("${sqs.queue.url}")
    private String queueUrl;

    /**
     * Polls the SQS queue for new messages at fixed intervals.
     * 
     * <p>Uses long polling (20s) to reduce empty responses and API costs.
     * After successful logging, messages are deleted from the queue.</p>
     * 
     * <p>Note: Business logic processing will be added in a future iteration.
     * Currently, messages are only logged and acknowledged (deleted).</p>
     */
    @Scheduled(fixedDelay = 1000)
    public void pollMessages() {
        log.debug("Polling SQS queue for messages: {}", queueUrl);

        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .waitTimeSeconds(LONG_POLLING_WAIT_TIME_SECONDS)
                .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            if (messages.isEmpty()) {
                log.debug("No messages received from SQS queue");
                return;
            }

            log.info("📥 Received {} message(s) from SQS queue", messages.size());

            for (Message message : messages) {
                processMessage(message);
            }

        } catch (Exception e) {
            log.error("❌ Error polling SQS queue: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes a single SQS message.
     * 
     * <p>Currently logs the message content and deletes it from the queue.
     * TODO: Implement business logic for event processing.</p>
     *
     * @param message the SQS message to process
     */
    private void processMessage(final Message message) {
        log.info("=== PROCESSING SQS MESSAGE ===");
        log.info("MessageId: {}", message.messageId());
        log.info("Body: {}", message.body());
        
        if (message.hasAttributes()) {
            log.debug("Attributes: {}", message.attributes());
        }
        
        if (message.hasMessageAttributes()) {
            log.debug("MessageAttributes: {}", message.messageAttributes());
        }

        // TODO: Add business logic here
        // Example: Parse EventBridge envelope, extract detail, process checkout event

        deleteMessage(message);
        log.info("=== MESSAGE PROCESSED ===");
    }

    /**
     * Deletes a message from the SQS queue after successful processing.
     *
     * @param message the message to delete
     */
    private void deleteMessage(final Message message) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

            sqsClient.deleteMessage(deleteRequest);
            log.debug("✅ Message deleted from queue: {}", message.messageId());

        } catch (Exception e) {
            log.error("❌ Failed to delete message {}: {}", message.messageId(), e.getMessage(), e);
        }
    }
}
