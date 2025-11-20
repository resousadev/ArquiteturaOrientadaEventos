package io.resousadev.linuxtips.mscheckout.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Configuration
@Slf4j
public class AwsConfig {
    
    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public EventBridgeClient eventBridgeClient() {
        log.info("Configurando EventBridgeClient");
        log.info("Região AWS: {}", awsRegion);
        
        try {
            var credentialsProvider = DefaultCredentialsProvider.builder().build();
            var credentials = credentialsProvider.resolveCredentials();
            log.info("Credenciais carregadas - Access Key ID: {}...", 
                credentials.accessKeyId().substring(0, 4));
        } catch (Exception e) {
            log.error("Erro ao carregar credenciais AWS", e);
        }
        
        return EventBridgeClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build();
    }
}
