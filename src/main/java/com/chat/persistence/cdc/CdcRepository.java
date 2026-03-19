package com.chat.persistence.cdc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CdcRepository extends JpaRepository<Cdc, Long> { }
