package io.resousadev.linuxtips.mscheckout.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable Spring's scheduled task execution.
 * 
 * <p>This configuration enables the {@link org.springframework.scheduling.annotation.Scheduled}
 * annotation support used by SQS message consumers and other scheduled tasks.</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Configuration class - enables @Scheduled annotation support
}
