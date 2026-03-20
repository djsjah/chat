package com.chat.core.admin.member;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.chat.api.AdminMembersApi;
import com.chat.core.admin.message.AdminMessageService;
import com.chat.model.AdminMessageMemberSliceDTO;
import com.chat.security.guard.AdminGuard;

@RestController
@RequestMapping("/api")
@AdminGuard
@RequiredArgsConstructor
public class AdminMemberController implements AdminMembersApi {
    private final AdminMemberService memberService;
    private final AdminMessageService messageService;

    @Override
    public AdminMessageMemberSliceDTO getMemberMessagesByIdForAdmin(Long memberId, Integer page, Integer size) {
        return messageService.getMessagesByMemberId(memberId, page, size);
    }

    @Override
    public void deleteMemberByIdForAdmin(Long memberId) { memberService.deleteOneById(memberId); }
}
