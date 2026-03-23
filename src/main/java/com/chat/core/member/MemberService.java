package com.chat.core.member;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.metric.ChatMetricService;
import com.chat.model.MemberResponseDTO;
import com.chat.persistence.member.Member;
import com.chat.persistence.member.repository.MemberRepository;
import com.chat.security.CurrentMemberProvider;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MemberService {
    private final CurrentMemberProvider currentMember;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatMetricService chatMetricService;
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Transactional(readOnly = true)
    public MemberResponseDTO getOneById(Long memberId) {
        return memberMapper.toResponseDTO(memberRepository.findAccessibleById(memberId, currentMember.subject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested member not found"))
        );
    }

    @Transactional
    public MemberResponseDTO create() {
        Member member = memberRepository.save(new Member(currentMember.subject(), currentMember.name()));
        MemberResponseDTO response = memberMapper.toResponseDTO(member);

        afterCommitExecutor.run(chatMetricService::markMemberCreated);
        return response;
    }
}
