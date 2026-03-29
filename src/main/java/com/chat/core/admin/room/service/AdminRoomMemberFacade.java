package com.chat.core.admin.room.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.event.ChatEvent;
import com.chat.app.service.ChatMetricService;
import com.chat.cdc.dto.CdcCreateDTO;
import com.chat.cdc.service.CdcInternalService;
import com.chat.core.admin.room.AdminRoomMapper;
import com.chat.core.member.event.MemberEventFactory;
import com.chat.core.member.event.payload.MemberEventPayload;
import com.chat.infrastructure.realtime.RealtimeUtils;
import com.chat.infrastructure.realtime.command.RealtimeApiMethod;
import com.chat.infrastructure.realtime.command.RealtimeChannelNamespace;
import com.chat.infrastructure.realtime.dto.PublishDTO;
import com.chat.model.AdminRoomResponseDTO;
import com.chat.model.AdminRoomWithMemberDTO;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.RoomMember;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminRoomMemberFacade {
    private final ObjectMapper objectMapper;
    private final ObservationRegistry observationRegistry;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatMetricService metricService;

    private final AdminRoomInternalService roomInternalService;
    private final AdminRoomMemberInternalService roomMemberInternalService;
    private final AdminRoomMapper roomMapper;

    private final CdcInternalService cdcInternalService;
    private final MemberEventFactory memberEventFactory;

    @Transactional
    public AdminRoomWithMemberDTO joinMemberToRoom(Long memberId, Long roomId) {
        return Observation.createNotStarted("chat.admin.room.member.join", observationRegistry)
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))
                .lowCardinalityKeyValue("member.id", String.valueOf(memberId))
                .lowCardinalityKeyValue("chat.event", "user_joined")

                .observe(() -> {
                    Room room = roomInternalService.getOneByIdForUpdate(roomId);
                    RoomMember roomMember = roomMemberInternalService.create(memberId, room);
                    room.setVersion(room.getVersion() + 1);

                    PublishDTO publishDTO = PublishDTO.builder()
                            .channel(RealtimeUtils.generateNamespacedChannel(
                                    RealtimeChannelNamespace.ROOM,
                                    room.getId()
                            ))
                            .data(memberEventFactory.createEvent(
                                    new MemberEventPayload(
                                            memberId.toString(),
                                            roomMember.getMember().getName(),
                                            ChatEvent.USER_JOINED,
                                            room
                                    )
                            ))
                            .idempotencyKey(RealtimeUtils.generateIdempotencyKey(
                                    ChatEvent.USER_JOINED,
                                    roomMember.getId()
                            ))
                            .build();

                    long partition = cdcInternalService.calcPartition(room.getId());
                    cdcInternalService.create(new CdcCreateDTO(
                            partition,
                            RealtimeApiMethod.PUBLISH,
                            objectMapper.convertValue(publishDTO, JsonNode.class)
                    ));

                    AdminRoomWithMemberDTO response = roomMapper.toResponseWithMemberDTO(room, roomMember.getMember());

                    log.info(
                            "Member joined room by admin: memberId={}, roomId={}, roomMemberId={}, roomVersion={}",
                            roomMember.getMember().getId(),
                            room.getId(),
                            roomMember.getId(),
                            room.getVersion()
                    );

                    log.debug(
                            "Realtime outbox saved: roomId={}, memberId={}, partition={}, channel={}, event={}",
                            room.getId(),
                            roomMember.getMember().getId(),
                            partition,
                            publishDTO.channel(),
                            ChatEvent.USER_JOINED
                    );

                    afterCommitExecutor.run(metricService::markMemberJoined);
                    return response;
                });
    }

    @Transactional
    public AdminRoomResponseDTO removeMemberFromRoom(Long memberId, Long roomId) {
        return Observation.createNotStarted("chat.admin.room.member.remove", observationRegistry)
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))
                .lowCardinalityKeyValue("member.id", String.valueOf(memberId))
                .lowCardinalityKeyValue("chat.event", "user_left")

                .observe(() -> {
                    Room room = roomInternalService.getAccessibleByIdForUpdate(roomId, memberId);
                    RoomMember roomMember = roomMemberInternalService.deleteOneByRoomIdAndMemberId(roomId, memberId);
                    room.setVersion(room.getVersion() + 1);

                    PublishDTO publishDTO = PublishDTO.builder()
                            .channel(RealtimeUtils.generateNamespacedChannel(
                                    RealtimeChannelNamespace.ROOM,
                                    room.getId()
                            ))
                            .data(memberEventFactory.createEvent(
                                    new MemberEventPayload(
                                            memberId.toString(),
                                            roomMember.getMember().getName(),
                                            ChatEvent.USER_LEFT,
                                            room
                                    )
                            ))
                            .idempotencyKey(RealtimeUtils.generateIdempotencyKey(
                                    ChatEvent.USER_LEFT, roomMember.getId()
                            ))
                            .build();

                    long partition = cdcInternalService.calcPartition(room.getId());
                    cdcInternalService.create(new CdcCreateDTO(
                            partition,
                            RealtimeApiMethod.PUBLISH,
                            objectMapper.convertValue(publishDTO, JsonNode.class)
                    ));

                    AdminRoomResponseDTO response = roomMapper.toResponseDTO(room);

                    log.info(
                            "Member removed from room by admin: memberId={}, roomId={}, roomMemberId={}, roomVersion={}",
                            roomMember.getMember().getId(),
                            room.getId(),
                            roomMember.getId(),
                            room.getVersion()
                    );

                    log.debug(
                            "Realtime outbox saved: roomId={}, memberId={}, partition={}, channel={}, event={}",
                            room.getId(),
                            roomMember.getMember().getId(),
                            partition,
                            publishDTO.channel(),
                            ChatEvent.USER_LEFT
                    );

                    afterCommitExecutor.run(metricService::markMemberLeft);
                    return response;
                });
    }
}
