package com.chat.core.message.event.payload;

import java.util.Objects;

import com.chat.app.event.ChatEvent;
import com.chat.persistence.message.Message;
import com.chat.persistence.room.Room;

public record MessageEventPayload(
        String memberSubject,
        String memberName,
        ChatEvent chatEvent,
        Message message,
        Room room
) {
    public MessageEventPayload {
        Objects.requireNonNull(memberSubject);
        Objects.requireNonNull(memberName);
        Objects.requireNonNull(chatEvent);
        Objects.requireNonNull(message);
        Objects.requireNonNull(room);
    }
}
