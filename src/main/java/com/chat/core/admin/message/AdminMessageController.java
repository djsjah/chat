package com.chat.core.admin.message;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.api.AdminMessagesApi;
import com.chat.model.AdminMessageExtendedDTO;
import com.chat.security.guard.AdminGuard;

@RestController
@RequestMapping("/api")
@AdminGuard
@RequiredArgsConstructor
public class AdminMessageController implements AdminMessagesApi {
    private final AdminMessageService messageService;

    @Override
    public AdminMessageExtendedDTO getMessageByIdForAdmin(Long id) { return messageService.getOneById(id); }
}
