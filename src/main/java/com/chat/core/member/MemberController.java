package com.chat.core.member;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.chat.api.MembersApi;
import com.chat.model.MemberResponseDTO;
import com.chat.security.guard.MemberGuard;

@RestController
@RequestMapping("/api")
@MemberGuard
@RequiredArgsConstructor
public class MemberController implements MembersApi {
    private final MemberService memberService;

    @Override
    public MemberResponseDTO getMemberById(Long memberId) { return memberService.getOneById(memberId); }

    @Override
    public MemberResponseDTO createMember() { return memberService.create(); }
}
