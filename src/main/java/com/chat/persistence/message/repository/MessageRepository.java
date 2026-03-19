package com.chat.persistence.message.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.chat.persistence.message.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByIdAndMemberSubject(Long id, String memberSubject);

    @Query("""
            SELECT m
            FROM Message m
            INNER JOIN RoomMember rm ON rm.room = m.room
            INNER JOIN rm.member current
            WHERE m.id = :messageId AND current.subject = :memberSubject AND current.isDeleted = false
            """)
    Optional<Message> findAccessibleById(
            @Param("messageId") Long messageId,
            @Param("memberSubject") String memberSubject
    );

    Optional<Message> findByIdAndRoomId(Long id, Long roomId);

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

    @Query(value = """
        SELECT *
        FROM message m
        WHERE m.room_id = :roomId
        ORDER BY m.created_at DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Message> findLatestByRoomId(@Param("roomId") Long roomId);
}
