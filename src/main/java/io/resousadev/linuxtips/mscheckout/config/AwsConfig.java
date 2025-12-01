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
        log.info("Configurando EventBridgeClient");
        logAwsConfiguration();

        EventBridgeClientBuilder builder = EventBridgeClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(buildCredentialsProvider());

        configureEndpointOverride(builder::endpointOverride, "EventBridge");

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        log.info("Configurando SqsClient");
        logAwsConfiguration();

        SqsClientBuilder builder = SqsClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(buildCredentialsProvider());

        configureEndpointOverride(builder::endpointOverride, "SQS");

        return builder.build();
    }

    /**
     * Builds the AWS credentials provider with the configured access key and secret.
     *
     * @return StaticCredentialsProvider with configured credentials
     */
    private StaticCredentialsProvider buildCredentialsProvider() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            accessKeyId,
            secretAccessKey
        );
        return StaticCredentialsProvider.create(awsCredentials);
    }

    /**
     * Configures endpoint override for LocalStack if the endpoint is configured.
     *
     * @param endpointOverrideSetter function to set the endpoint override on the builder
     * @param serviceName name of the AWS service for logging purposes
     */
    private void configureEndpointOverride(
            final java.util.function.Consumer<URI> endpointOverrideSetter,
            final String serviceName) {
        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            log.info("🔧 {} usando endpoint customizado (LocalStack): {}", serviceName, awsEndpoint);
            endpointOverrideSetter.accept(URI.create(awsEndpoint));
        } else {
            log.info("☁️ {} usando endpoint AWS real", serviceName);
        }
    }

    /**
     * Logs AWS configuration details for debugging purposes.
     */
    private void logAwsConfiguration() {
        log.info("Região AWS: {}", awsRegion);

        if (accessKeyId != null && accessKeyId.length() >= 4) {
            log.info("Access Key ID: {}...", accessKeyId.substring(0, 4));
        } else {
            log.warn("Access Key ID não configurada ou inválida");
        }
    }
}
