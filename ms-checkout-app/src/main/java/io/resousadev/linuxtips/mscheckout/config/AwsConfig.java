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
        log.info("Região AWS: {}", awsRegion);

        // Proteção contra credenciais vazias
        if (accessKeyId != null && accessKeyId.length() >= 4) {
            log.info("Access Key ID: {}...", accessKeyId.substring(0, 4));
        } else {
            log.warn("Access Key ID não configurada ou inválida");
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
            log.info("🔧 Usando endpoint customizado (LocalStack): {}", awsEndpoint);
            builder.endpointOverride(URI.create(awsEndpoint));
        } else {
            log.info("☁️ Usando endpoint AWS real");
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        log.info("Configurando SqsClient");

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            accessKeyId,
            secretAccessKey
        );
        
        SqsClientBuilder builder = SqsClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

        // Configure endpoint override for LocalStack
        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            log.info("🔧 SQS usando endpoint customizado (LocalStack): {}", awsEndpoint);
            builder.endpointOverride(URI.create(awsEndpoint));
        } else {
            log.info("☁️ SQS usando endpoint AWS real");
        }

        return builder.build();
    }
}
