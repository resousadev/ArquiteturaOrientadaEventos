package io.resousadev.linuxtips.mscheckout.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Configuration
@Slf4j
public class AwsConfig {
    
    @Value("${aws.region}")
    private String awsRegion;
    
    @Value("${aws.accessKeyId}")
    private String accessKeyId;
    
    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

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
        
        return EventBridgeClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();
    }
}
