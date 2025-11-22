package io.resousadev.linuxtips.mscheckout;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest
@ActiveProfiles("test")
class MsCheckoutApplicationTests {

	// Mock do EventBridgeClient para evitar tentativa de conexão real com AWS durante os testes
	@MockitoBean
	private EventBridgeClient eventBridgeClient;

	@Test
	void contextLoads() {
		// Este teste verifica se o contexto Spring carrega corretamente
		// Com o EventBridgeClient mockado, não há tentativa de conexão com AWS
	}

}
