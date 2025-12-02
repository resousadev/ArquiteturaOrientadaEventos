package io.resousadev.linuxtips.managerfile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Manager File microservice.
 * This is an independent microservice for file management operations.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>REST API for file upload/download/delete operations</li>
 *   <li>AWS S3 integration for file storage</li>
 *   <li>EventBridge integration for publishing file events</li>
 *   <li>SQS integration for consuming events from other microservices</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class ManagerFileApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ManagerFileApplication.class, args);
    }
}
