package com.chat.cdc.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.AfterCommitExecutor;
import com.chat.cdc.CdcProperties;
import com.chat.cdc.dto.CdcCreateDTO;
import com.chat.persistence.cdc.Cdc;
import com.chat.persistence.cdc.CdcRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CdcInternalService {
    private final AfterCommitExecutor afterCommitExecutor;
    private final CdcRepository cdcRepository;
    private final CdcProperties props;
    private final CdcMetricService cdcMetricService;

    public <T> long calcPartition(T entityId) { return Math.abs(entityId.hashCode() % props.partitions()); }

    @Transactional(propagation = Propagation.MANDATORY)
    public void create(CdcCreateDTO createDTO) {
        cdcRepository.save(
                new Cdc(createDTO.partition(), createDTO.method(), createDTO.payload())
        );

        afterCommitExecutor.run(cdcMetricService::markCreated);
    }
}
