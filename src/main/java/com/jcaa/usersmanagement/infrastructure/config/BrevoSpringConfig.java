package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "email.provider", havingValue = "brevo", matchIfMissing = true)
public class BrevoSpringConfig {

    @Bean
    public BrevoConfig brevoConfig(
            @Value("${brevo.base-url}") final String baseUrl,
            @Value("${brevo.local.api-key:${brevo.api-key}}") final String apiKey,
            @Value("${brevo.local.from.address:${brevo.from.address}}") final String fromAddress,
            @Value("${brevo.local.from.name:${brevo.from.name}}") final String fromName,
            @Value("${brevo.timeout.connect}") final Duration connectTimeout,
            @Value("${brevo.timeout.read}") final Duration readTimeout,
            @Value("${brevo.retry.max-attempts}") final int retryMaxAttempts,
            @Value("${brevo.retry.initial-delay}") final Duration retryInitialDelay,
            @Value("${brevo.retry.multiplier}") final double retryMultiplier,
            @Value("${brevo.circuit-breaker.failure-rate-threshold}") final int failureThreshold,
            @Value("${brevo.circuit-breaker.sliding-window-size}") final int windowSize,
            @Value("${brevo.circuit-breaker.minimum-calls}") final int minimumCalls,
            @Value("${brevo.circuit-breaker.open-duration}") final Duration openDuration) {
        return new BrevoConfig(baseUrl, apiKey, fromAddress, fromName, connectTimeout, readTimeout,
                retryMaxAttempts, retryInitialDelay, retryMultiplier,
                failureThreshold, windowSize, minimumCalls, openDuration);
    }

    @Bean
    public RestClient brevoRestClient(final BrevoConfig config) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.connectTimeout());
        factory.setReadTimeout(config.readTimeout());
        return RestClient.builder()
                .baseUrl(config.baseUrl())
                .requestFactory(factory)
                .defaultHeader("api-key", config.apiKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}