package com.chat.persistence.room.repository.admin;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.chat.persistence.room.Room;

public interface AdminRoomRepository extends JpaRepository<Room, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Room r
            WHERE r.id = :id
            """)
    Optional<Room> findOneByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Room r
            INNER JOIN RoomMember rm on rm.room = r
            INNER JOIN rm.member m
            WHERE r.id = :id AND m.id = :memberId AND m.isDeleted = false
            """)
    Optional<Room> findAccessibleByIdForUpdate(@Param("id") Long id, @Param("memberId") Long memberId);
}
