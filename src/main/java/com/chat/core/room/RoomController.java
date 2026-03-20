package com.chat.core.room;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.chat.api.RoomsApi;
import com.chat.core.message.service.MessageFacade;
import com.chat.core.message.service.MessageService;
import com.chat.model.MessageCreateDTO;
import com.chat.model.MessageRoomSliceDTO;
import com.chat.model.MessageWithRoomDTO;
import com.chat.security.guard.MemberGuard;

@RestController
@RequestMapping("/api")
@MemberGuard
@RequiredArgsConstructor
public class RoomController implements RoomsApi {
    private final MessageService messageService;
    private final MessageFacade messageFacade;

    @Override
    public MessageRoomSliceDTO getMessagesByRoomId(Long roomId, Integer page, Integer size) {
        return messageService.getMessagesByRoomId(roomId, page, size);
    }

    @Override
    public MessageWithRoomDTO createMessage(Long roomId, MessageCreateDTO createDTO) {
        return messageFacade.createMessage(roomId, createDTO);
    }
}
