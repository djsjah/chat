package com.chat.persistence.room.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.chat.persistence.room.RoomMember;

public interface AdminRoomMemberRepository extends JpaRepository<RoomMember, Long> {
    Optional<RoomMember> findByRoomIdAndMemberId(Long roomId, Long memberId);
}
