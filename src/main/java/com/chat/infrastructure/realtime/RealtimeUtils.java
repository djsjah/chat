package com.chat.infrastructure.realtime;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.chat.app.event.ChatEvent;
import com.chat.infrastructure.realtime.command.RealtimeChannelNamespace;

public final class RealtimeUtils {
    private RealtimeUtils() { }

    public static String generateNamespacedChannel(RealtimeChannelNamespace channelNamespace, Object entityId) {
        return channelNamespace + ":" + entityId;
    }

    public static String generateIdempotencyKey(ChatEvent chatEvent, Object entityId) {
        return generateIdempotencyKey(chatEvent, entityId, null);
    }

    public static String generateIdempotencyKey(ChatEvent chatEvent, Object entityId, OffsetDateTime timestamp) {
        Objects.requireNonNull(chatEvent, "chatEvent");
        Objects.requireNonNull(entityId, "entityId");

        if (timestamp != null) {
            return chatEvent + "_" + entityId + "_" + timestamp.toInstant().toEpochMilli();
        }
        return chatEvent + "_" + entityId;
    }
}
