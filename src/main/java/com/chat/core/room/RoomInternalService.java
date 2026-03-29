package com.chat.core.room;

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
import com.chat.persistence.room.repository.RoomRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RoomInternalService {
    private final ObservationRegistry observationRegistry;
    private final RoomRepository roomRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getAccessibleByIdForUpdate(Long roomId, String memberSubject) {
        return Observation.createNotStarted("chat.room.resolve-for-update", observationRegistry)
                .lowCardinalityKeyValue("room.id", String.valueOf(roomId))
                .lowCardinalityKeyValue("resolve.by", "room-id")

                .observe(() -> roomRepository.findAccessibleByIdForUpdate(roomId, memberSubject)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Requested room not found"
                        )));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Room getAccessibleByMessageIdForUpdate(Long messageId, String memberSubject) {
        return Observation.createNotStarted("chat.room.resolve-for-update", observationRegistry)
                .lowCardinalityKeyValue("message.id", String.valueOf(messageId))
                .lowCardinalityKeyValue("resolve.by", "message-id")

                .observe(() -> roomRepository.findAccessibleByMessageIdForUpdate(messageId, memberSubject)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Requested room not found"
                        )));
    }
}
