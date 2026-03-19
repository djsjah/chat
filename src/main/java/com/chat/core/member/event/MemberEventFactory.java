package com.chat.core.member.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.chat.core.member.event.payload.MemberEventPayload;
import com.chat.infrastructure.realtime.RealtimeEvent;
import com.chat.persistence.room.Room;

@Component
@RequiredArgsConstructor
public class MemberEventFactory {
    private final ObjectMapper objectMapper;

    public RealtimeEvent createEvent(MemberEventPayload payload) {
        return new RealtimeEvent(
                payload.chatEvent(),
                createEventBody(payload.memberSubject(), payload.memberName(), payload.room())
        );
    }

    private JsonNode createEventBody(String memberSubject, String memberName, Room room) {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("id", room.getId());
        root.put("version", room.getVersion());
        root.put("bumpedAt", String.valueOf(room.getBumpedAt()));

        ObjectNode member = objectMapper.createObjectNode();
        member.put("subject", memberSubject);
        member.put("name", memberName);

        root.set("member", member);
        return root;
    }
}
