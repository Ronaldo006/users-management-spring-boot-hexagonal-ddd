package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@DisplayName("AsyncNotificationConfig")
class AsyncNotificationConfigTest {

    @Test
    @DisplayName("notificationExecutor() debe crear el pool con el nombre y tamano esperados")
    void shouldCreateConfiguredExecutor() {
        final Executor executor = new AsyncNotificationConfig().notificationExecutor();

        final ThreadPoolTaskExecutor pool = assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        try {
            assertEquals("email-notification-", pool.getThreadNamePrefix());
            assertEquals(2, pool.getCorePoolSize());
            assertEquals(4, pool.getMaxPoolSize());
        } finally {
            pool.shutdown();
        }
    }
}