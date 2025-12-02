package io.resousadev.linuxtips.mscheckout;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Base class for integration tests that require PostgreSQL via Testcontainers.
 *
 * <p>Tests extending this class will be automatically skipped if Docker is not available,
 * thanks to {@code disabledWithoutDocker = true} in {@link Testcontainers}.</p>
 *
 * <p>This allows developers without Docker to still run unit tests while integration
 * tests are properly executed in CI/CD environments.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DockerAvailableCondition.class)
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("ms_checkout_test")
        .withUsername("test")
        .withPassword("test");

    // Mock do EventBridgeClient para evitar conexão real com AWS durante testes
    @MockitoBean
    protected EventBridgeClient eventBridgeClient;

    // Mock do SqsClient para evitar conexão real com AWS durante testes
    @MockitoBean
    protected SqsClient sqsClient;

}
