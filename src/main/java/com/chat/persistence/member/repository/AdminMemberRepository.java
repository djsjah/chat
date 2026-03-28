package com.chat.persistence.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.chat.persistence.member.Member;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminMemberRepository extends JpaRepository<Member, Long> {
    @Query("""
            SELECT m
            FROM Member m
            WHERE m.id = :memberId AND m.isDeleted = false
            """)
    Optional<Member> findActiveOneById(@Param("memberId") Long memberId);
}
