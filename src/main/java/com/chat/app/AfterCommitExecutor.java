package com.chat.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class AfterCommitExecutor {
    public void run(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            log.warn("After-commit action executed immediately because no active transaction was found");
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
