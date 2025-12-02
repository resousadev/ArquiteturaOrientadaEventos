package io.resousadev.linuxtips.mscheckout;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 extension that disables tests if Docker is not available.
 *
 * <p>This extension checks if Docker daemon is accessible before running tests.
 * If Docker is not available, the test is skipped with an informative message.</p>
 */
public class DockerAvailableCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
        ConditionEvaluationResult.enabled("Docker is available");

    private static final ConditionEvaluationResult DISABLED =
        ConditionEvaluationResult.disabled(
            "Docker is not available. Start Docker Desktop to run integration tests.");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        return isDockerAvailable() ? ENABLED : DISABLED;
    }

    private boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
