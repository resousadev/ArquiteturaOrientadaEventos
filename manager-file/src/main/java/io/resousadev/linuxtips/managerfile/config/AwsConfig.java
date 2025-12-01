package io.resousadev.linuxtips.managerfile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * AWS SDK configuration for S3, EventBridge, SQS clients and S3Presigner.
 * 
 * <p>Uses StaticCredentialsProvider when explicit credentials are provided (local/test),
 * otherwise falls back to DefaultCredentialsProvider for production environments.</p>
 * 
 * @see <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html">AWS SDK Credentials</a>
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.endpoint:}")
    private String awsEndpoint;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    /**
     * Creates the appropriate credentials provider based on configuration.
     * Uses static credentials for local/test environments, DefaultCredentialsProvider for production.
     */
    private AwsCredentialsProvider buildCredentialsProvider() {
        if (accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        }
        // Use default credentials chain for production (IAM roles, env vars, etc.)
        return DefaultCredentialsProvider.builder()
                .asyncCredentialUpdateEnabled(true)
                .build();
    }

    private boolean hasCustomEndpoint() {
        return awsEndpoint != null && !awsEndpoint.isBlank();
    }

    @Bean
    public S3Client s3Client() {
        final var builder = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(buildCredentialsProvider());

        if (hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(awsEndpoint));
            builder.forcePathStyle(true);
        }

        return builder.build();
    }

    /**
     * S3Presigner bean for generating pre-signed URLs.
     * Must use same region and credentials as S3Client.
     * 
     * @see <a href="https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/presigner/S3Presigner.html">S3Presigner</a>
     */
    @Bean
    public S3Presigner s3Presigner() {
        final var builder = S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(buildCredentialsProvider());

        if (hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }

    @Bean
    public EventBridgeClient eventBridgeClient() {
        final var builder = EventBridgeClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(buildCredentialsProvider());

        if (hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        final var builder = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(buildCredentialsProvider());

        if (hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }
}
