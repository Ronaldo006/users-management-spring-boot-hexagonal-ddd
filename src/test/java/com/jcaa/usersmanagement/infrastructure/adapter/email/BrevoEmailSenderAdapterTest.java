package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Tests del adaptador de Brevo. MockRestServiceServer evita cualquier llamada de red real. */
@DisplayName("BrevoEmailSenderAdapter")
class BrevoEmailSenderAdapterTest {

    private static final String BASE_URL = "http://brevo.test";
    private static final String SEND_URL = BASE_URL + "/v3/smtp/email";
    private static final String API_KEY = "xkeysib-test";

    private static final EmailDestinationModel DESTINATION =
            new EmailDestinationModel("john@example.com", "John Doe", "Asunto", "<p>Hola</p>");

    private static BrevoConfig config(final int maxAttempts, final int minimumCalls) {
        return new BrevoConfig(
                BASE_URL,
                API_KEY,
                "remitente@example.com",
                "Gestion de Usuarios",
                Duration.ofMillis(100),
                Duration.ofMillis(100),
                maxAttempts,
                Duration.ofMillis(1),
                1.0,
                50,
                minimumCalls,
                minimumCalls,
                Duration.ofSeconds(30));
    }

    /** Crea el RestClient con la misma cabecera api-key que BrevoSpringConfig. */
    private static RestClient.Builder clientBuilder() {
        return RestClient.builder().baseUrl(BASE_URL).defaultHeader("api-key", API_KEY);
    }

    @Test
    @DisplayName("debe enviar el JSON correcto a Brevo con la cabecera api-key")
    void shouldPostExpectedPayload() {
        final RestClient.Builder builder = clientBuilder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server
                .expect(requestTo(SEND_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", API_KEY))
                .andExpect(jsonPath("$.sender.email").value("remitente@example.com"))
                .andExpect(jsonPath("$.sender.name").value("Gestion de Usuarios"))
                .andExpect(jsonPath("$.to[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.to[0].name").value("John Doe"))
                .andExpect(jsonPath("$.subject").value("Asunto"))
                .andExpect(jsonPath("$.htmlContent").value("<p>Hola</p>"))
                .andExpect(jsonPath("$.headers.idempotencyKey").exists())
                .andRespond(withSuccess());
        final BrevoEmailSenderAdapter adapter =
                new BrevoEmailSenderAdapter(config(1, 5), builder.build());

        assertDoesNotThrow(() -> adapter.send(DESTINATION));

        server.verify();
    }

    @Test
    @DisplayName("debe reintentar ante un error 500 y fallar con HTTP 500 al agotar los intentos")
    void shouldRetryOnServerErrorAndThenFail() {
        final RestClient.Builder builder = clientBuilder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server
                .expect(ExpectedCount.times(3), requestTo(SEND_URL))
                .andRespond(withServerError());
        final BrevoEmailSenderAdapter adapter =
                new BrevoEmailSenderAdapter(config(3, 100), builder.build());

        final EmailSenderException exception =
                assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));

        assertTrue(exception.getMessage().contains("HTTP 500"));
        server.verify();
    }

    @Test
    @DisplayName("debe reintentar ante un error 429 (demasiadas peticiones)")
    void shouldRetryOnTooManyRequests() {
        final RestClient.Builder builder = clientBuilder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server
                .expect(ExpectedCount.times(2), requestTo(SEND_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        final BrevoEmailSenderAdapter adapter =
                new BrevoEmailSenderAdapter(config(2, 100), builder.build());

        final EmailSenderException exception =
                assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));

        assertTrue(exception.getMessage().contains("HTTP 429"));
        server.verify();
    }

    @Test
    @DisplayName("no debe reintentar ante un error 400 (error del cliente)")
    void shouldNotRetryOnBadRequest() {
        final RestClient.Builder builder = clientBuilder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(ExpectedCount.once(), requestTo(SEND_URL)).andRespond(withBadRequest());
        final BrevoEmailSenderAdapter adapter =
                new BrevoEmailSenderAdapter(config(3, 100), builder.build());

        final EmailSenderException exception =
                assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));

        assertTrue(exception.getMessage().contains("HTTP 400"));
        server.verify();
    }

    @Test
    @DisplayName("debe traducir un error de red a EmailSenderException")
    void shouldWrapNetworkError() {
        final RestClient.Builder builder = clientBuilder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server
                .expect(ExpectedCount.once(), requestTo(SEND_URL))
                .andRespond(withException(new IOException("sin red")));
        final BrevoEmailSenderAdapter adapter =
                new BrevoEmailSenderAdapter(config(1, 100), builder.build());

        final EmailSenderException exception =
                assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));

        assertTrue(exception.getMessage().contains("ResourceAccessException"));
        server.verify();
    }

    @Test
    @DisplayName("debe abrir el circuit breaker tras varios fallos y dejar de llamar a Brevo")
    void shouldOpenCircuitBreakerAfterRepeatedFailures() {
        final RestClient.Builder builder = clientBuilder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(ExpectedCount.times(2), requestTo(SEND_URL)).andRespond(withServerError());
        final BrevoEmailSenderAdapter adapter =
                new BrevoEmailSenderAdapter(config(1, 2), builder.build());

        assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));
        assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));
        final EmailSenderException exception =
                assertThrows(EmailSenderException.class, () -> adapter.send(DESTINATION));

        assertTrue(exception.getMessage().contains("circuit breaker abierto"));
        server.verify();
    }
}
