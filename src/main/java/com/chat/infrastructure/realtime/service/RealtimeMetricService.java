package com.chat.infrastructure.realtime.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RealtimeMetricService {
    private final MeterRegistry meterRegistry;

    private Timer connectionTokenTimer;
    private Timer subscriptionTokenTimer;

    private Counter connectionTokenGenerated;
    private Counter subscriptionTokenGenerated;
    private Counter subscriptionTokenErrors;

    @PostConstruct
    void init() {
        connectionTokenTimer = Timer.builder("chat_realtime_connection_token_seconds")
                .description("Time spent generating realtime connection token")
                .register(meterRegistry);

        subscriptionTokenTimer = Timer.builder("chat_realtime_subscription_token_seconds")
                .description("Time spent generating realtime subscription token")
                .register(meterRegistry);

        connectionTokenGenerated = Counter.builder("chat_realtime_connection_token_generated_total")
                .description("Total number of generated realtime connection tokens")
                .register(meterRegistry);

        subscriptionTokenGenerated = Counter.builder("chat_realtime_subscription_token_generated_total")
                .description("Total number of generated realtime subscription tokens")
                .register(meterRegistry);

        subscriptionTokenErrors = Counter.builder("chat_realtime_subscription_token_errors_total")
                .description("Total number of realtime subscription token generation errors")
                .register(meterRegistry);
    }

    public <T> T recordConnectionToken(Supplier<T> action) { return connectionTokenTimer.record(action); }
    public <T> T recordSubscriptionToken(Supplier<T> action) { return subscriptionTokenTimer.record(action); }

    public void markConnectionTokenGenerated() { connectionTokenGenerated.increment(); }
    public void markSubscriptionTokenGenerated() { subscriptionTokenGenerated.increment(); }
    public void markSubscriptionTokenError() { subscriptionTokenErrors.increment(); }
}
