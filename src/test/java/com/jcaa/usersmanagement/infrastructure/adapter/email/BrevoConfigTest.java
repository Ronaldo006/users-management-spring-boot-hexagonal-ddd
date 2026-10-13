package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BrevoConfig")
class BrevoConfigTest {

    private static final String BASE_URL = "https://api.brevo.com";
    private static final String API_KEY = "xkeysib-test";
    private static final String FROM = "remitente@example.com";

    private static BrevoConfig build(final String baseUrl, final String apiKey, final String from) {
        return new BrevoConfig(
                baseUrl,
                apiKey,
                from,
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
    @DisplayName("debe crear la configuracion cuando los datos son validos")
    void shouldCreateConfigWhenValuesAreValid() {
        final BrevoConfig config = build(BASE_URL, API_KEY, FROM);

        assertAll(
                () -> assertEquals(BASE_URL, config.baseUrl()),
                () -> assertEquals(API_KEY, config.apiKey()),
                () -> assertEquals(FROM, config.fromAddress()),
                () -> assertEquals(3, config.retryMaxAttempts()));
    }

    @Test
    @DisplayName("debe fallar cuando la base url esta vacia")
    void shouldFailWhenBaseUrlIsBlank() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> build(" ", API_KEY, FROM));

        assertTrue(exception.getMessage().contains("brevo.base-url"));
    }

    @Test
    @DisplayName("debe fallar cuando la api key es nula")
    void shouldFailWhenApiKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> build(BASE_URL, null, FROM));
    }

    @Test
    @DisplayName("debe fallar cuando el remitente esta vacio")
    void shouldFailWhenFromAddressIsBlank() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> build(BASE_URL, API_KEY, ""));

        assertTrue(exception.getMessage().contains("BREVO_FROM_ADDRESS"));
    }

    @Test
    @DisplayName("debe fallar cuando la api key no tiene el prefijo xkeysib-")
    void shouldFailWhenApiKeyHasWrongPrefix() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> build(BASE_URL, "xsmtpsib-abc", FROM));

        assertTrue(exception.getMessage().contains("xkeysib-"));
    }
}