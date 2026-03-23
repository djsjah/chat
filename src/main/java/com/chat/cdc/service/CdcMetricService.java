package com.chat.cdc.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class CdcMetricService {
    private final MeterRegistry meterRegistry;

    private final AtomicLong rowsCurrent = new AtomicLong();
    private final AtomicLong oldestRowAgeSeconds = new AtomicLong();

    private Counter createdTotal;

    @PostConstruct
    void init() {
        createdTotal = Counter.builder("chat_cdc_created_total")
                .description("Total number of created CDC records")
                .register(meterRegistry);

        Gauge.builder("chat_cdc_rows_current", rowsCurrent, AtomicLong::get)
                .description("Current number of rows in cdc table")
                .register(meterRegistry);

        Gauge.builder("chat_cdc_oldest_row_age_seconds", oldestRowAgeSeconds, AtomicLong::get)
                .description("Age in seconds of the oldest row in cdc table")
                .register(meterRegistry);
    }

    public void markCreated() { createdTotal.increment(); }

    public void setRowsCurrent(long value) { rowsCurrent.set(value); }
    public void setOldestRowAgeSeconds(long value) { oldestRowAgeSeconds.set(value); }
}
