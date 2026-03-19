package com.chat.core.admin.room.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.event.ChatEvent;
import com.chat.cdc.CdcInternalService;
import com.chat.cdc.dto.CdcCreateDTO;
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
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminRoomMemberFacade {
    private final ObjectMapper objectMapper;

    private final AdminRoomInternalService roomInternalService;
    private final AdminRoomMemberInternalService roomMemberInternalService;
    private final AdminRoomMapper roomMapper;

    private final CdcInternalService cdcInternalService;
    private final MemberEventFactory memberEventFactory;

    @Transactional
    public AdminRoomWithMemberDTO joinMemberToRoom(Long memberId, Long roomId) {
        Room room = roomInternalService.getAccessibleByIdForUpdate(roomId, memberId);
        RoomMember roomMember = roomMemberInternalService.create(memberId, room);
        room.setVersion(room.getVersion() + 1);

        PublishDTO publishDTO = PublishDTO.builder()
                .channel(RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, room.getId()))
                .data(memberEventFactory.createEvent(
                        new MemberEventPayload(
                                memberId.toString(),
                                roomMember.getMember().getName(),
                                ChatEvent.USER_JOINED,
                                room
                        )
                ))
                .idempotencyKey(RealtimeUtils.generateIdempotencyKey(ChatEvent.USER_JOINED, roomMember.getId()))
                .build();

        cdcInternalService.create(new CdcCreateDTO(
                cdcInternalService.calcPartition(room.getId()),
                RealtimeApiMethod.PUBLISH,
                objectMapper.convertValue(publishDTO, JsonNode.class)
        ));

        return roomMapper.toResponseWithMemberDTO(room, roomMember.getMember());
    }

    @Transactional
    public AdminRoomResponseDTO removeMemberFromRoom(Long memberId, Long roomId) {
        Room room = roomInternalService.getAccessibleByIdForUpdate(roomId, memberId);
        RoomMember roomMember = roomMemberInternalService.deleteOneByRoomIdAndMemberId(roomId, memberId);
        room.setVersion(room.getVersion() + 1);

        PublishDTO publishDTO = PublishDTO.builder()
                .channel(RealtimeUtils.generateNamespacedChannel(RealtimeChannelNamespace.ROOM, room.getId()))
                .data(memberEventFactory.createEvent(
                        new MemberEventPayload(
                                memberId.toString(),
                                roomMember.getMember().getName(),
                                ChatEvent.USER_LEFT,
                                room
                        )
                ))
                .idempotencyKey(RealtimeUtils.generateIdempotencyKey(ChatEvent.USER_LEFT, roomMember.getId()))
                .build();

        cdcInternalService.create(new CdcCreateDTO(
                cdcInternalService.calcPartition(room.getId()),
                RealtimeApiMethod.PUBLISH,
                objectMapper.convertValue(publishDTO, JsonNode.class)
        ));

        return roomMapper.toResponseDTO(room);
    }
}
