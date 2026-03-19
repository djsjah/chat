package com.chat.persistence.room.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.chat.persistence.room.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Room r
            INNER JOIN RoomMember rm on rm.room = r
            INNER JOIN rm.member m
            WHERE r.id = :id AND m.subject = :subject AND m.isDeleted = false
            """)
    Optional<Room> findAccessibleByIdForUpdate(@Param("id") Long id, @Param("subject") String subject);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT DISTINCT r
            FROM Room r
            INNER JOIN Message msg ON msg.room = r
            INNER JOIN msg.member author
            INNER JOIN RoomMember rm ON rm.room = r
            INNER JOIN rm.member current
            WHERE msg.id = :messageId
              AND author.subject = :subject
              AND author.isDeleted = false
              AND current.subject = :subject
              AND current.isDeleted = false
            """)
    Optional<Room> findAccessibleByMessageIdForUpdate(
            @Param("messageId") Long messageId,
            @Param("subject") String subject
    );
}
