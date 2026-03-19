package com.chat.core.message.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

import com.chat.app.event.ChatEvent;
import com.chat.cdc.CdcInternalService;
import com.chat.cdc.dto.CdcCreateDTO;
import com.chat.core.message.MessageMapper;
import com.chat.core.message.event.MessageEventFactory;
import com.chat.core.message.event.payload.MessageEventPayload;
import com.chat.core.room.RoomInternalService;
import com.chat.infrastructure.realtime.RealtimeUtils;
import com.chat.infrastructure.realtime.command.RealtimeApiMethod;
import com.chat.infrastructure.realtime.command.RealtimeChannelNamespace;
import com.chat.infrastructure.realtime.dto.PublishDTO;
import com.chat.model.MessageCreateDTO;
import com.chat.model.MessagePatchDTO;
import com.chat.model.MessageWithRoomDTO;
import com.chat.persistence.message.Message;
import com.chat.persistence.room.Room;
import com.chat.security.CurrentMemberProvider;

@Service
@RequiredArgsConstructor
public class MessageFacade {
    private final ObjectMapper objectMapper;
    private final CurrentMemberProvider currentMember;

    private final MessageInternalService messageInternalService;
    private final MessageEventFactory messageEventFactory;
    private final MessageMapper messageMapper;

    private final RoomInternalService roomInternalService;
    private final CdcInternalService cdcInternalService;

    @Transactional
    public MessageWithRoomDTO createMessage(Long roomId, MessageCreateDTO createDTO) {
        Room room = roomInternalService.getAccessibleByIdForUpdate(roomId, currentMember.subject());
        Message newMessage = messageInternalService.create(currentMember.subject(), room, createDTO);
        room.applyNewMessage(newMessage, OffsetDateTime.now(Clock.systemUTC()));

        PublishDTO publishDTO = PublishDTO.builder()
                .channel(RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, room.getId()))
                .data(messageEventFactory.createEvent(
                        new MessageEventPayload(
                                currentMember.subject(),
                                currentMember.name(),
                                ChatEvent.MESSAGE_ADDED,
                                newMessage,
                                room
                        )
                ))
                .idempotencyKey(RealtimeUtils.generateIdempotencyKey(ChatEvent.MESSAGE_ADDED, newMessage.getId()))
                .build();

        cdcInternalService.create(new CdcCreateDTO(
                cdcInternalService.calcPartition(room.getId()),
                RealtimeApiMethod.PUBLISH,
                objectMapper.convertValue(publishDTO, JsonNode.class)
        ));

        return messageMapper.toResponseWithRoomDTO(newMessage, room);
    }

    @Transactional
    public MessageWithRoomDTO patchMessageById(Long messageId, MessagePatchDTO patchDTO) {
        Room room = roomInternalService.getAccessibleByMessageIdForUpdate(messageId, currentMember.subject());
        Message updatedMessage = messageInternalService.patchOneById(messageId, currentMember.subject(), patchDTO);

        room.setVersion(room.getVersion() + 1);

        PublishDTO publishDTO = PublishDTO.builder()
                .channel(RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, room.getId()))
                .data(messageEventFactory.createEvent(
                        new MessageEventPayload(
                                currentMember.subject(),
                                currentMember.name(),
                                ChatEvent.MESSAGE_UPDATED,
                                updatedMessage,
                                room
                        )
                ))
                .idempotencyKey(RealtimeUtils.generateIdempotencyKey(
                        ChatEvent.MESSAGE_UPDATED,
                        updatedMessage.getId(),
                        updatedMessage.getUpdatedAt()
                ))
                .build();

        cdcInternalService.create(new CdcCreateDTO(
                cdcInternalService.calcPartition(room.getId()),
                RealtimeApiMethod.PUBLISH,
                objectMapper.convertValue(publishDTO, JsonNode.class)
        ));

        return messageMapper.toResponseWithRoomDTO(updatedMessage, room);
    }

    @Transactional
    public MessageWithRoomDTO deleteMessageById(Long messageId) {
        Room room = roomInternalService.getAccessibleByMessageIdForUpdate(messageId, currentMember.subject());
        Message deletedMessage = messageInternalService.deleteOneById(messageId, currentMember.subject());

        room.setVersion(room.getVersion() + 1);
        if (room.getLastMessage() != null && room.getLastMessage().getId().equals(deletedMessage.getId())) {
            room.setLastMessage(messageInternalService.getLatestByRoomId(room.getId()));
        }

        PublishDTO publishDTO = PublishDTO.builder()
                .channel(RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, room.getId()))
                .data(messageEventFactory.createEvent(
                        new MessageEventPayload(
                                currentMember.subject(),
                                currentMember.name(),
                                ChatEvent.MESSAGE_DELETED,
                                deletedMessage,
                                room
                        )
                ))
                .idempotencyKey(RealtimeUtils.generateIdempotencyKey(
                        ChatEvent.MESSAGE_DELETED,
                        deletedMessage.getId()
                ))
                .build();

        cdcInternalService.create(new CdcCreateDTO(
                cdcInternalService.calcPartition(room.getId()),
                RealtimeApiMethod.PUBLISH,
                objectMapper.convertValue(publishDTO, JsonNode.class)
        ));

        return messageMapper.toResponseWithRoomDTO(deletedMessage, room);
    }
}
