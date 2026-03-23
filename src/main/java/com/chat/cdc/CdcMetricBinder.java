package com.chat.cdc;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

import com.chat.cdc.service.CdcMetricService;
import com.chat.persistence.cdc.CdcRepository;

@Component
@RequiredArgsConstructor
public class CdcMetricBinder {
    private final CdcRepository cdcRepository;
    private final CdcMetricService cdcMetricService;

    @Scheduled(fixedDelay = 3600000)
    @Transactional(readOnly = true)
    public void refresh() {
        cdcMetricService.setRowsCurrent(cdcRepository.count());

        OffsetDateTime oldestCreatedAt = cdcRepository.findOldestCreatedAt();
        if (oldestCreatedAt == null) {
            cdcMetricService.setOldestRowAgeSeconds(0);
            return;
        }

        long ageSeconds = Duration.between(oldestCreatedAt, OffsetDateTime.now(Clock.systemUTC())).getSeconds();
        cdcMetricService.setOldestRowAgeSeconds(Math.max(ageSeconds, 0));
    }
}
