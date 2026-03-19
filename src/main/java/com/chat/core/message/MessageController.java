package com.chat.core.message;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.chat.api.MessagesApi;
import com.chat.core.message.service.MessageFacade;
import com.chat.core.message.service.MessageService;
import com.chat.model.MessagePatchDTO;
import com.chat.model.MessageResponseDTO;
import com.chat.model.MessageWithRoomDTO;
import com.chat.security.guard.MemberGuard;

@RestController
@RequestMapping("/api/messages")
@MemberGuard
@RequiredArgsConstructor
public class MessageController implements MessagesApi {
    private final MessageService messageService;
    private final MessageFacade messageFacade;

    @Override
    public MessageResponseDTO getMessageById(Long messageId) { return messageService.getOneById(messageId); }

    @Override
    public MessageWithRoomDTO patchMessageById(Long messageId, MessagePatchDTO patchDTO) {
        return messageFacade.patchMessageById(messageId, patchDTO);
    }

    @Override
    public MessageWithRoomDTO deleteMessageById(Long messageId) { return messageFacade.deleteMessageById(messageId); }
}
