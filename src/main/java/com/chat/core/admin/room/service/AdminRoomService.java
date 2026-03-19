package com.chat.core.admin.room.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.core.admin.room.AdminRoomMapper;
import com.chat.model.AdminRoomResponseDTO;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.admin.AdminRoomRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminRoomService {
    private final AdminRoomRepository roomRepository;
    private final AdminRoomMapper roomMapper;

    @Transactional
    public AdminRoomResponseDTO create() {
        return roomMapper.toResponseDTO(
                roomRepository.save(Room.create())
        );
    }
}
