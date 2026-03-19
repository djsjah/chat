package com.chat.core.message.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.chat.infrastructure.realtime.RealtimeEvent;
import com.chat.core.message.event.payload.MessageEventPayload;
import com.chat.persistence.message.Message;
import com.chat.persistence.room.Room;

@Component
@RequiredArgsConstructor
public class MessageEventFactory {
    private final ObjectMapper objectMapper;

    public RealtimeEvent createEvent(MessageEventPayload payload) {
        return new RealtimeEvent(
                payload.chatEvent(),
                createEventBody(
                        payload.memberSubject(),
                        payload.memberName(),
                        payload.message(),
                        payload.room()
                )
        );
    }

    private JsonNode createEventBody(String memberSubject, String memberName, Message message, Room room) {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("id", message.getId());
        root.put("content", message.getContent());
        root.put("createdAt", message.getCreatedAt().toString());
        root.put("updatedAt", message.getUpdatedAt().toString());

        ObjectNode roomNode = objectMapper.createObjectNode();
        roomNode.put("id", room.getId());
        roomNode.put("version", room.getVersion());
        roomNode.put("bumpedAt", room.getBumpedAt().toString());

        roomNode.set("lastMessage", objectMapper.valueToTree(room.getLastMessage()));
        root.set("room", roomNode);

        ObjectNode memberNode = objectMapper.createObjectNode();
        memberNode.put("subject", memberSubject);
        memberNode.put("memberName", memberName);

        root.set("member", memberNode);

        return root;
    }
}
