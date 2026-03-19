package com.chat.core.room;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.RoomRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RoomInternalService {
    private final RoomRepository roomRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getAccessibleByIdForUpdate(Long roomId, String memberSubject) {
        return roomRepository.findAccessibleByIdForUpdate(roomId, memberSubject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found"));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getAccessibleByMessageIdForUpdate(Long messageId, String memberSubject) {
        return roomRepository.findAccessibleByMessageIdForUpdate(messageId, memberSubject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found"));
    }
}
