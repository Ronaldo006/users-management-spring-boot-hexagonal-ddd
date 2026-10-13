package com.jcaa.usersmanagement.infrastructure.adapter.email;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "brevo", matchIfMissing = true)
public class BrevoEmailSenderAdapter implements EmailSenderPort {

    private final BrevoConfig config;
    private final RestClient restClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public BrevoEmailSenderAdapter(final BrevoConfig config, final RestClient brevoRestClient) {
        this.config = config;
        this.restClient = brevoRestClient;
        this.retry = Retry.of("brevo-email", RetryConfig.custom()
                .maxAttempts(config.retryMaxAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        config.retryInitialDelay().toMillis(), config.retryMultiplier()))
                .retryOnException(BrevoEmailSenderAdapter::isTransientFailure)
                .build());
        this.circuitBreaker = CircuitBreaker.of("brevo-email", CircuitBreakerConfig.custom()
                .failureRateThreshold(config.circuitFailureThreshold())
                .slidingWindowSize(config.circuitSlidingWindowSize())
                .minimumNumberOfCalls(config.circuitMinimumCalls())
                .waitDurationInOpenState(config.circuitOpenDuration())
                .recordException(BrevoEmailSenderAdapter::isTransientFailure)
                .build());
    }

    @Override
    public void send(final EmailDestinationModel destination) {
        final BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Contact(config.fromAddress(), config.fromName()),
                List.of(new BrevoEmailRequest.Contact(
                        destination.getDestinationEmail(), destination.getDestinationName())),
                destination.getSubject(),
                destination.getBody(),
                Map.of("idempotencyKey", UUID.randomUUID().toString()));
        final Runnable call = Retry.decorateRunnable(retry,
                CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                        restClient.post().uri("/v3/smtp/email").body(request).retrieve().toBodilessEntity()));
        try {
            call.run();
            log.info("[BrevoEmailSenderAdapter] correo aceptado por Brevo para {}.",
                    destination.getDestinationEmail());
        } catch (final RestClientResponseException e) {
            throw EmailSenderException.becauseApiFailed(
                    destination.getDestinationEmail(), "HTTP " + e.getStatusCode().value());
        } catch (final CallNotPermittedException e) {
            throw EmailSenderException.becauseApiFailed(
                    destination.getDestinationEmail(), "circuit breaker abierto");
        } catch (final RestClientException e) {
            throw EmailSenderException.becauseApiFailed(
                    destination.getDestinationEmail(), e.getClass().getSimpleName());
        }
    }

    private static boolean isTransientFailure(final Throwable e) {
        if (e instanceof ResourceAccessException || e instanceof HttpServerErrorException) {
            return true;
        }
        if (e instanceof HttpClientErrorException c) {
            return c.getStatusCode().value() == 408 || c.getStatusCode().value() == 429;
        }
        return false;
    }
}