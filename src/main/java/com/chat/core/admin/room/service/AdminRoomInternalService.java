package com.chat.core.admin.room.service;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
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
    private final ObservationRegistry observationRegistry;
    private final AdminRoomRepository roomRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getOneByIdForUpdate(Long roomId) {
        return Observation.createNotStarted("chat.admin.room.resolve-for-update", observationRegistry)
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))
                .lowCardinalityKeyValue("resolve.by", "room-id")

                .observe(() -> roomRepository.findOneByIdForUpdate(roomId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Requested room not found"
                        )));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getAccessibleByIdForUpdate(Long roomId, Long memberId) {
        return Observation.createNotStarted("chat.admin.room.resolve-for-update", observationRegistry)
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))
                .lowCardinalityKeyValue("member.id", String.valueOf(memberId))
                .lowCardinalityKeyValue("resolve.by", "room-and-member")

                .observe(() -> roomRepository.findAccessibleByIdForUpdate(roomId, memberId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Requested room not found"
                        )));
    }
}
