package com.chat.core.admin.room.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.persistence.member.Member;
import com.chat.persistence.member.repository.AdminMemberRepository;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.RoomMember;
import com.chat.persistence.room.repository.admin.AdminRoomMemberRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminRoomMemberInternalService {
    private final AdminMemberRepository memberRepository;
    private final AdminRoomMemberRepository roomMemberRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public RoomMember create(Long memberId, Room room) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested member not found"));

        return roomMemberRepository.save(new RoomMember(room, member));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public RoomMember deleteOneByRoomIdAndMemberId(Long roomId, Long memberId) {
        RoomMember roomMember = roomMemberRepository.findByRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room member not found"));

        roomMemberRepository.delete(roomMember);
        return roomMember;
    }
}
