package com.chat.persistence.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.chat.persistence.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("""
        SELECT DISTINCT target
        FROM Member target
        INNER JOIN RoomMember rmTarget ON rmTarget.member = target
        INNER JOIN RoomMember rmCurrent ON rmCurrent.room = rmTarget.room
        INNER JOIN rmCurrent.member current
        WHERE target.id = :memberId
          AND current.subject = :currentMemberSubject
          AND current.isDeleted = false
    """)
    Optional<Member> findAccessibleById(
            @Param("memberId") Long memberId,
            @Param("currentMemberSubject") String currentMemberSubject
    );
}
