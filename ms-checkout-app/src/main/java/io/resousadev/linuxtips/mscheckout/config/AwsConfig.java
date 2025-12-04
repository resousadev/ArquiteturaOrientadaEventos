package io.resousadev.linuxtips.mscheckout.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
@Slf4j
public class AwsConfig {
    
    @Value("${aws.region}")
    private String awsRegion;
    
    @Value("${aws.accessKeyId}")
    private String accessKeyId;
    
    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.endpoint:}")
    private String awsEndpoint;

    @Bean
    public EventBridgeClient eventBridgeClient() {
        log.info("Initializing EventBridgeClient: region={}", awsRegion);

        if (accessKeyId == null || accessKeyId.length() < 4) {
            log.warn("AWS credentials may be invalid: accessKeyId is null or too short");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            accessKeyId,
            secretAccessKey
        );
        
        EventBridgeClientBuilder builder = EventBridgeClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

        // Configure endpoint override for LocalStack
        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            log.info("EventBridgeClient configured with custom endpoint: endpoint={}", awsEndpoint);
            builder.endpointOverride(URI.create(awsEndpoint));
        } else {
            log.debug("EventBridgeClient configured with AWS production endpoint");
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        log.info("Initializing SqsClient: region={}", awsRegion);

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            accessKeyId,
            secretAccessKey
        );
        
        SqsClientBuilder builder = SqsClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

        // Configure endpoint override for LocalStack
        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            log.info("SqsClient configured with custom endpoint: endpoint={}", awsEndpoint);
            builder.endpointOverride(URI.create(awsEndpoint));
        } else {
            log.debug("SqsClient configured with AWS production endpoint");
        }

        return builder.build();
    }
}
