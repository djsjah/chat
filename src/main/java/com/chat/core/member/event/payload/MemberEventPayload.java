package com.chat.core.member.event.payload;

import java.util.Objects;

import com.chat.app.event.ChatEvent;
import com.chat.persistence.room.Room;

public record MemberEventPayload(
        String memberSubject,
        String memberName,
        ChatEvent chatEvent,
        Room room
) {
    public MemberEventPayload {
        Objects.requireNonNull(memberSubject);
        Objects.requireNonNull(memberName);
        Objects.requireNonNull(chatEvent);
        Objects.requireNonNull(room);
    }
}
