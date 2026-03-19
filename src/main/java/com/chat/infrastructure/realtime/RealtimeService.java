package com.chat.infrastructure.realtime;

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

import com.chat.infrastructure.realtime.command.RealtimeChannelNamespace;
import com.chat.persistence.room.repository.RoomMemberRepository;
import com.chat.security.CurrentMemberProvider;

@Service
public class RealtimeService {
    public static final long EXP_CONNECTION_TIME_OFFSET_SECONDS = 120L;
    public static final long EXP_SUBSCRIPTION_TIME_OFFSET_SECONDS = 300L;

    private final CurrentMemberProvider currentMember;
    private final SecretKey secretKey;
    private final RoomMemberRepository roomMemberRepository;

    public RealtimeService(
            CurrentMemberProvider currentMember,
            @Value("${realtime-server.secret-key}") String secretKey,
            RoomMemberRepository roomMemberRepository
    ) {
        this.currentMember = currentMember;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.roomMemberRepository = roomMemberRepository;
    }

    public String generateConnectionToken() {
        String subject = currentMember.subject();
        Instant exp = Instant.now().plusSeconds(EXP_CONNECTION_TIME_OFFSET_SECONDS);

        return Jwts.builder()
                .subject(subject)
                .expiration(Date.from(exp))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateSubscriptionToken(Long roomId) {
        String subject = currentMember.subject();
        if (!roomMemberRepository.existsByRoomIdAndMemberSubject(roomId, subject)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found");
        }

        Instant exp = Instant.now().plusSeconds(EXP_SUBSCRIPTION_TIME_OFFSET_SECONDS);
        String channel = RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, roomId);

        return Jwts.builder()
                .subject(subject)
                .expiration(Date.from(exp))
                .claim("channel", channel)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
}
