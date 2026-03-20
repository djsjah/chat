package com.chat.core.admin.room;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.chat.api.AdminRoomsApi;
import com.chat.core.admin.message.AdminMessageService;
import com.chat.core.admin.room.service.AdminRoomMemberFacade;
import com.chat.core.admin.room.service.AdminRoomService;
import com.chat.model.AdminMessageRoomSliceDTO;
import com.chat.model.AdminRoomResponseDTO;
import com.chat.model.AdminRoomWithMemberDTO;
import com.chat.security.guard.AdminGuard;

@RestController
@RequestMapping("/api")
@AdminGuard
@RequiredArgsConstructor
public class AdminRoomController implements AdminRoomsApi {
    private final AdminRoomService roomService;
    private final AdminRoomMemberFacade roomMemberFacade;
    private final AdminMessageService messageService;

    @Override
    public AdminMessageRoomSliceDTO getRoomMessagesByRoomIdForAdmin(Long roomId, Integer page, Integer size) {
        return messageService.getMessagesByRoomId(roomId, page, size);
    }

    @Override
    public AdminRoomResponseDTO createRoomForAdmin() { return roomService.create(); }

    @Override
    public AdminRoomWithMemberDTO joinMemberToRoomForAdmin(Long roomId, Long memberId) {
        return roomMemberFacade.joinMemberToRoom(memberId, roomId);
    }

    @Override
    public AdminRoomResponseDTO removeMemberFromRoomForAdmin(Long roomId, Long memberId) {
        return roomMemberFacade.removeMemberFromRoom(memberId, roomId);
    }
}
