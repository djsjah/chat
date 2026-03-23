package com.chat.infrastructure.realtime.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
public class RealtimeService {
    public static final long EXP_CONNECTION_TIME_OFFSET_SECONDS = 120L;
    public static final long EXP_SUBSCRIPTION_TIME_OFFSET_SECONDS = 300L;

    private final CurrentMemberProvider currentMember;
    private final SecretKey secretKey;
    private final RealtimeMetricService realtimeMetricService;
    private final RoomMemberRepository roomMemberRepository;

    public RealtimeService(
            CurrentMemberProvider currentMember,
            @Value("${realtime-server.secret-key}") String secretKey,
            RealtimeMetricService realtimeMetricService,
            RoomMemberRepository roomMemberRepository
    ) {
        this.currentMember = currentMember;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.realtimeMetricService = realtimeMetricService;
        this.roomMemberRepository = roomMemberRepository;
    }

    public String generateConnectionToken() {
        return realtimeMetricService.recordConnectionToken(() -> {
            String subject = currentMember.subject();
            Instant exp = Instant.now().plusSeconds(EXP_CONNECTION_TIME_OFFSET_SECONDS);

            String token = Jwts.builder()
                    .subject(subject)
                    .expiration(Date.from(exp))
                    .signWith(secretKey, Jwts.SIG.HS256)
                    .compact();

            realtimeMetricService.markConnectionTokenGenerated();
            return token;
        });
    }

    public String generateSubscriptionToken(Long roomId) {
        return realtimeMetricService.recordSubscriptionToken(() -> {
            try {
                String subject = currentMember.subject();

                if (!roomMemberRepository.existsByRoomIdAndMemberSubject(roomId, subject)) {
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
                return token;
            } catch (RuntimeException ex) {
                realtimeMetricService.markSubscriptionTokenError();
                throw ex;
            }
        });
    }
}
