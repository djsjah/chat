package com.chat.infrastructure.realtime.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import com.chat.infrastructure.realtime.RealtimeUtils;
import com.chat.infrastructure.realtime.command.RealtimeChannelNamespace;
import com.chat.persistence.room.repository.RoomMemberRepository;
import com.chat.security.CurrentMemberProvider;

@Service
@Slf4j
public class RealtimeService {
    public static final long EXP_CONNECTION_TIME_OFFSET_SECONDS = 120L;
    public static final long EXP_SUBSCRIPTION_TIME_OFFSET_SECONDS = 300L;

    private final ObservationRegistry observationRegistry;
    private final CurrentMemberProvider currentMember;
    private final SecretKey secretKey;
    private final RealtimeMetricService realtimeMetricService;
    private final RoomMemberRepository roomMemberRepository;

    public RealtimeService(
            ObservationRegistry observationRegistry,
            CurrentMemberProvider currentMember,
            @Value("${realtime-server.secret-key}") String secretKey,
            RealtimeMetricService realtimeMetricService,
            RoomMemberRepository roomMemberRepository
    ) {
        this.currentMember = currentMember;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.realtimeMetricService = realtimeMetricService;
        this.roomMemberRepository = roomMemberRepository;
        this.observationRegistry = observationRegistry;
    }

    public String generateConnectionToken() {
        return Observation.createNotStarted("chat.realtime.connection-token.generate", observationRegistry)
                .lowCardinalityKeyValue("chat.token.type", "connection")

                .observe(() -> realtimeMetricService.recordConnectionToken(() -> {
                    String subject = currentMember.subject();
                    Instant exp = Instant.now().plusSeconds(EXP_CONNECTION_TIME_OFFSET_SECONDS);

                    String token = Jwts.builder()
                            .subject(subject)
                            .expiration(Date.from(exp))
                            .signWith(secretKey, Jwts.SIG.HS256)
                            .compact();

                    realtimeMetricService.markConnectionTokenGenerated();

                    log.info("Realtime connection token generated: memberSubject={}", subject);
                    return token;
                }));
    }

    public String generateSubscriptionToken(Long roomId) {
        return Observation.createNotStarted("chat.realtime.subscription-token.generate", observationRegistry)
                .lowCardinalityKeyValue("chat.token.type", "subscription")
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))

                .observe(() -> realtimeMetricService.recordSubscriptionToken(() -> {
                    String subject = currentMember.subject();

                    try {
                        if (!roomMemberRepository.existsByRoomIdAndMemberSubject(roomId, subject)) {
                            log.warn(
                                    "Realtime subscription token generation failed: memberSubject={}, roomId={}",
                                    subject,
                                    roomId
                            );
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found");
                        }

                        Instant exp = Instant.now().plusSeconds(EXP_SUBSCRIPTION_TIME_OFFSET_SECONDS);
                        String channel = RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, roomId);

                        String token = Jwts.builder()
                                .subject(subject)
                                .expiration(Date.from(exp))
                                .claim("channel", channel)
                                .signWith(secretKey, Jwts.SIG.HS256)
                                .compact();

                        realtimeMetricService.markSubscriptionTokenGenerated();

                        log.info(
                                "Realtime subscription token generated: memberSubject={}, roomId={}, channel={}",
                                subject,
                                roomId,
                                channel
                        );

                        return token;
                    } catch (RuntimeException ex) {
                        realtimeMetricService.markSubscriptionTokenError();
                        throw ex;
                    }
                }));
    }
}
