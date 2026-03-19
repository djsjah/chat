package com.chat.core.admin.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.persistence.member.Member;
import com.chat.persistence.member.repository.AdminMemberRepository;

@Service
@RequiredArgsConstructor
public class AdminMemberService {
    private final AdminMemberRepository memberRepository;

    @Transactional
    public void deleteOneById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested member not found"));

        member.setDeleted(true);
        memberRepository.save(member);
    }
}
