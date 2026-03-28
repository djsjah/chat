package com.chat.cdc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import com.chat.cdc.service.CdcMetricService;
import com.chat.persistence.cdc.CdcRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class CdcMetricBinder {
    private static final long CDC_BACKLOG_WARN_THRESHOLD_SECONDS = 3600;

    private final CdcRepository cdcRepository;
    private final CdcMetricService cdcMetricService;

    private final AtomicBoolean backlogWarningActive = new AtomicBoolean(false);

    @Scheduled(fixedDelay = 3600000)
    @Transactional(readOnly = true)
    public void refresh() {
        long rowsCurrent = cdcRepository.count();
        cdcMetricService.setRowsCurrent(rowsCurrent);

        OffsetDateTime oldestCreatedAt = cdcRepository.findOldestCreatedAt();
        if (oldestCreatedAt == null) {
            cdcMetricService.setOldestRowAgeSeconds(0);

            if (backlogWarningActive.compareAndSet(true, false)) {
                log.info("CDC backlog cleared: rowsCurrent=0, oldestRowAgeSeconds=0");
            }
            return;
        }

        long ageSeconds = Duration.between(oldestCreatedAt, OffsetDateTime.now(Clock.systemUTC())).getSeconds();
        ageSeconds = Math.max(ageSeconds, 0);
        cdcMetricService.setOldestRowAgeSeconds(ageSeconds);

        if (ageSeconds >= CDC_BACKLOG_WARN_THRESHOLD_SECONDS) {
            if (backlogWarningActive.compareAndSet(false, true)) {
                log.warn(
                        "CDC backlog detected: rowsCurrent={}, oldestRowAgeSeconds={}, thresholdSeconds={}",
                        rowsCurrent,
                        ageSeconds,
                        CDC_BACKLOG_WARN_THRESHOLD_SECONDS
                );
            }
        } else {
            if (backlogWarningActive.compareAndSet(true, false)) {
                log.info(
                        "CDC backlog normalized: rowsCurrent={}, oldestRowAgeSeconds={}, thresholdSeconds={}",
                        rowsCurrent,
                        ageSeconds,
                        CDC_BACKLOG_WARN_THRESHOLD_SECONDS
                );
            }
        }
    }
}
