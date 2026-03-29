package com.chat.core.admin.member;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.service.ChatMetricService;
import com.chat.persistence.member.Member;
import com.chat.persistence.member.repository.AdminMemberRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminMemberService {
    private final ObservationRegistry observationRegistry;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatMetricService chatMetricService;
    private final AdminMemberRepository memberRepository;

    @Transactional
    public void deleteOneById(Long id) {
        Observation.createNotStarted("chat.admin.member.delete", observationRegistry)
                .lowCardinalityKeyValue("chat.operation", "soft-delete")
                .lowCardinalityKeyValue("member.id", String.valueOf(id))

                .observe(() -> {
                    Member member = memberRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Requested member not found"
                            ));

                    member.setDeleted(true);
                    memberRepository.save(member);

                    log.info(
                            "Member soft-deleted by admin: memberId={}, memberSubject={}, memberName={}",
                            member.getId(),
                            member.getSubject(),
                            member.getName()
                    );

                    afterCommitExecutor.run(chatMetricService::markMemberDeleted);
                });
    }
}
