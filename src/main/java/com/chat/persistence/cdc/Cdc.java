package com.chat.persistence.cdc;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.chat.infrastructure.realtime.command.RealtimeApiMethod;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class Cdc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, updatable = false, columnDefinition = "realtime_server_api_method")
    private RealtimeApiMethod method = RealtimeApiMethod.PUBLISH;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(nullable = false, updatable = false)
    private long partition = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Cdc(long partition, RealtimeApiMethod method, JsonNode payload) {
        this.partition = partition;
        this.method = Objects.requireNonNull(method);
        this.payload = Objects.requireNonNull(payload);
    }
}
