package com.safetrip.backend.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para habilitar procesamiento asíncrono
 * Necesario para que @Async funcione correctamente
 */
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // Número mínimo de threads
        executor.setMaxPoolSize(10);  // Número máximo de threads
        executor.setQueueCapacity(25); // Capacidad de cola
        executor.setThreadNamePrefix("async-payment-");
        executor.initialize();
        return executor;
    }
}