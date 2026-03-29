package com.chat.core.message.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.event.ChatEvent;
import com.chat.app.service.ChatMetricService;
import com.chat.cdc.dto.CdcCreateDTO;
import com.chat.cdc.service.CdcInternalService;
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
@Slf4j
@RequiredArgsConstructor
public class MessageFacade {
    private final ObjectMapper objectMapper;
    private final ObservationRegistry observationRegistry;
    private final CurrentMemberProvider currentMember;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatMetricService metricService;

    private final MessageInternalService messageInternalService;
    private final MessageEventFactory messageEventFactory;
    private final MessageMapper messageMapper;

    private final RoomInternalService roomInternalService;
    private final CdcInternalService cdcInternalService;

    @Transactional
    public MessageWithRoomDTO createMessage(Long roomId, MessageCreateDTO createDTO) {
        return Observation.createNotStarted("chat.message.create", observationRegistry)
                .lowCardinalityKeyValue("chat.operation", "create")
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))
                .lowCardinalityKeyValue("has.parent", String.valueOf(createDTO.getParentId() != null))
                .lowCardinalityKeyValue("chat.event", "message_added")

                .observe(() -> {
                    Room room = roomInternalService.getAccessibleByIdForUpdate(roomId, currentMember.subject());
                    Message newMessage = messageInternalService.create(currentMember.subject(), room, createDTO);
                    room.applyNewMessage(newMessage, OffsetDateTime.now(Clock.systemUTC()));

                    PublishDTO publishDTO = PublishDTO.builder()
                            .channel(RealtimeUtils.generateNamespacedChannel(
                                    RealtimeChannelNamespace.ROOM,
                                    room.getId()
                            ))
                            .data(messageEventFactory.createEvent(
                                    new MessageEventPayload(
                                            currentMember.subject(),
                                            currentMember.name(),
                                            ChatEvent.MESSAGE_ADDED,
                                            newMessage,
                                            room
                                    )
                            ))
                            .idempotencyKey(RealtimeUtils.generateIdempotencyKey(
                                    ChatEvent.MESSAGE_ADDED,
                                    newMessage.getId())
                            )
                            .build();

                    long partition = cdcInternalService.calcPartition(room.getId());
                    cdcInternalService.create(new CdcCreateDTO(
                            partition,
                            RealtimeApiMethod.PUBLISH,
                            objectMapper.convertValue(publishDTO, JsonNode.class)
                    ));

                    MessageWithRoomDTO response = messageMapper.toResponseWithRoomDTO(newMessage, room);

                    log.info(
                            "Message created: memberSubject={}, roomId={}, messageId={}, parentId={}, roomVersion={}",
                            currentMember.subject(),
                            room.getId(),
                            newMessage.getId(),
                            newMessage.getParent() != null ? newMessage.getParent().getId() : null,
                            room.getVersion()
                    );

                    log.debug(
                            "Realtime outbox saved: roomId={}, messageId={}, partition={}, channel={}, event={}",
                            room.getId(),
                            newMessage.getId(),
                            partition,
                            publishDTO.channel(),
                            ChatEvent.MESSAGE_ADDED
                    );

                    afterCommitExecutor.run(metricService::markMessageCreated);
                    return response;
                });
    }

    @Transactional
    public MessageWithRoomDTO patchMessageById(Long messageId, MessagePatchDTO patchDTO) {
        return Observation.createNotStarted("chat.message.patch", observationRegistry)
                .lowCardinalityKeyValue("chat.operation", "patch")
                .lowCardinalityKeyValue("message.id", String.valueOf(messageId))
                .lowCardinalityKeyValue("chat.event", "message_updated")

                .observe(() -> {
                    Room room = roomInternalService.getAccessibleByMessageIdForUpdate(messageId, currentMember.subject());
                    Message updatedMessage = messageInternalService.patchOneById(
                            messageId,
                            currentMember.subject(),
                            patchDTO
                    );

                    room.setVersion(room.getVersion() + 1);

                    PublishDTO publishDTO = PublishDTO.builder()
                            .channel(RealtimeUtils.generateNamespacedChannel(
                                    RealtimeChannelNamespace.ROOM,
                                    room.getId()
                            ))
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

                    long partition = cdcInternalService.calcPartition(room.getId());
                    cdcInternalService.create(new CdcCreateDTO(
                            partition,
                            RealtimeApiMethod.PUBLISH,
                            objectMapper.convertValue(publishDTO, JsonNode.class)
                    ));

                    MessageWithRoomDTO response = messageMapper.toResponseWithRoomDTO(updatedMessage, room);

                    log.info(
                            "Message updated: memberSubject={}, roomId={}, messageId={}, roomVersion={}",
                            currentMember.subject(),
                            room.getId(),
                            updatedMessage.getId(),
                            room.getVersion()
                    );

                    log.debug(
                            "Realtime outbox saved: roomId={}, messageId={}, partition={}, channel={}, event={}",
                            room.getId(),
                            updatedMessage.getId(),
                            partition,
                            publishDTO.channel(),
                            ChatEvent.MESSAGE_UPDATED
                    );

                    afterCommitExecutor.run(metricService::markMessageUpdated);
                    return response;
                });
    }

    @Transactional
    public MessageWithRoomDTO deleteMessageById(Long messageId) {
        return Observation.createNotStarted("chat.message.delete", observationRegistry)
                .lowCardinalityKeyValue("chat.operation", "delete")
                .lowCardinalityKeyValue("message.id", String.valueOf(messageId))
                .lowCardinalityKeyValue("chat.event", "message_deleted")

                .observe(() -> {
                    Room room = roomInternalService.getAccessibleByMessageIdForUpdate(
                            messageId,
                            currentMember.subject()
                    );
                    Message deletedMessage = messageInternalService.deleteOneById(messageId, currentMember.subject());

                    room.setVersion(room.getVersion() + 1);
                    if (room.getLastMessage() != null && room.getLastMessage().getId().equals(deletedMessage.getId())) {
                        room.setLastMessage(messageInternalService.getLatestByRoomId(room.getId()));
                    }

                    PublishDTO publishDTO = PublishDTO.builder()
                            .channel(RealtimeUtils.generateNamespacedChannel(
                                    RealtimeChannelNamespace.ROOM,
                                    room.getId()
                            ))
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

                    long partition = cdcInternalService.calcPartition(room.getId());
                    cdcInternalService.create(new CdcCreateDTO(
                            partition,
                            RealtimeApiMethod.PUBLISH,
                            objectMapper.convertValue(publishDTO, JsonNode.class)
                    ));

                    MessageWithRoomDTO response = messageMapper.toResponseWithRoomDTO(deletedMessage, room);

                    log.info(
                            "Message deleted: memberSubject={}, roomId={}, messageId={}, newLastMessageId={}, roomVersion={}",
                            currentMember.subject(),
                            room.getId(),
                            deletedMessage.getId(),
                            room.getLastMessage() != null ? room.getLastMessage().getId() : null,
                            room.getVersion()
                    );

                    log.debug(
                            "Realtime outbox saved: roomId={}, messageId={}, partition={}, channel={}, event={}",
                            room.getId(),
                            deletedMessage.getId(),
                            partition,
                            publishDTO.channel(),
                            ChatEvent.MESSAGE_DELETED
                    );

                    afterCommitExecutor.run(metricService::markMessageDeleted);
                    return response;
                });
    }
}
