package com.chat.persistence.cdc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

public interface CdcRepository extends JpaRepository<Cdc, Long> {
    @Query("SELECT MIN(c.createdAt) FROM Cdc c")
    OffsetDateTime findOldestCreatedAt();
}
