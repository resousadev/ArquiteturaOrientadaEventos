package io.resousadev.linuxtips.mscheckout;

import org.junit.jupiter.api.Test;

/**
 * Integration test that verifies the Spring application context loads correctly.
 * Uses PostgreSQL via Testcontainers for database integration.
 *
 * <p>This test will be automatically skipped if Docker is not available.</p>
 */
class MsCheckoutApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
		// Verifica se o contexto Spring carrega corretamente
		// com PostgreSQL via Testcontainers e EventBridgeClient mockado
	}

}
