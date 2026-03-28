package com.chat.core.admin.room.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.admin.AdminRoomRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AdminRoomInternalService {
    private final AdminRoomRepository roomRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getOneByIdForUpdate(Long roomId) {
        return roomRepository.findOneByIdForUpdate(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found"));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getAccessibleByIdForUpdate(Long roomId, Long memberId) {
        return roomRepository.findAccessibleByIdForUpdate(roomId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found"));
    }
}
