package com.chat.persistence.message.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chat.persistence.message.Message;

public interface AdminMessageRepository extends JpaRepository<Message, Long> {
    @Query("""
        SELECT DISTINCT m
        FROM Message m
        INNER JOIN FETCH m.member
        LEFT JOIN FETCH m.parent p
        LEFT JOIN FETCH p.member
        WHERE m.room.id = :roomId
        ORDER BY m.createdAt DESC
    """)
    Slice<Message> findSliceByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    @Query("""
        SELECT DISTINCT m
        FROM Message m
        INNER JOIN FETCH m.member mb
        LEFT JOIN FETCH m.parent p
        LEFT JOIN FETCH p.room
        WHERE mb.id = :memberId
        ORDER BY m.createdAt DESC
    """)
    Slice<Message> findSliceByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
