package com.chat.persistence.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.chat.persistence.room.RoomMember;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    boolean existsByRoomIdAndMemberSubject(Long roomId, String subject);

    @Query("""
            SELECT rm
            FROM RoomMember rm
            INNER JOIN FETCH rm.member m
            INNER JOIN rm.room r
            WHERE r.id = :roomId AND m.subject = :memberSubject
            """)
    Optional<RoomMember> findWithMemberByRoomIdAndMemberSubject(
            @Param("roomId") Long roomId,
            @Param("memberSubject") String memberSubject
    );
}
