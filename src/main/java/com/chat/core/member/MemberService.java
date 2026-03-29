package com.chat.core.member;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.service.ChatMetricService;
import com.chat.model.MemberResponseDTO;
import com.chat.persistence.member.Member;
import com.chat.persistence.member.repository.MemberRepository;
import com.chat.security.CurrentMemberProvider;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MemberService {
    private final ObservationRegistry observationRegistry;
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
        return Observation.createNotStarted("chat.member.create", observationRegistry)
                .lowCardinalityKeyValue("chat.operation", "create")

                .observe(() -> {
                    Member member = memberRepository.save(new Member(currentMember.subject(), currentMember.name()));
                    MemberResponseDTO response = memberMapper.toResponseDTO(member);

                    log.info(
                            "Member created: memberSubject={}, memberId={}, memberName={}",
                            member.getSubject(),
                            member.getId(),
                            member.getName()
                    );

                    afterCommitExecutor.run(chatMetricService::markMemberCreated);
                    return response;
                });
    }
}
