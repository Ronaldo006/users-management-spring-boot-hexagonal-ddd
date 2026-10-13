package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("BrevoSpringConfig")
class BrevoSpringConfigTest {

    private final BrevoSpringConfig springConfig = new BrevoSpringConfig();

    private BrevoConfig buildConfig(final String apiKey) {
        return springConfig.brevoConfig(
                "https://api.brevo.com",
                apiKey,
                "remitente@example.com",
                "Gestion de Usuarios",
                Duration.ofSeconds(2),
                Duration.ofSeconds(5),
                3,
                Duration.ofMillis(500),
                2.0,
                50,
                10,
                5,
                Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("brevoConfig() debe construir la configuracion con los valores recibidos")
    void shouldBuildBrevoConfig() {
        final BrevoConfig config = buildConfig("xkeysib-test");

        assertAll(
                () -> assertEquals("https://api.brevo.com", config.baseUrl()),
                () -> assertEquals("xkeysib-test", config.apiKey()),
                () -> assertEquals("remitente@example.com", config.fromAddress()),
                () -> assertEquals(Duration.ofSeconds(30), config.circuitOpenDuration()));
    }

    @Test
    @DisplayName("brevoConfig() debe fallar si la api key no es valida")
    void shouldFailWhenApiKeyIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> buildConfig("clave-invalida"));
    }

    @Test
    @DisplayName("brevoRestClient() debe crear el cliente HTTP")
    void shouldBuildRestClient() {
        final RestClient client = springConfig.brevoRestClient(buildConfig("xkeysib-test"));

        assertNotNull(client);
    }
}