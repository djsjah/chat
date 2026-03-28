package com.chat.core.admin.room.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.service.ChatMetricService;
import com.chat.core.admin.room.AdminRoomMapper;
import com.chat.model.AdminRoomResponseDTO;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.admin.AdminRoomRepository;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminRoomService {
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatMetricService chatMetricService;
    private final AdminRoomRepository roomRepository;
    private final AdminRoomMapper roomMapper;

    @Transactional
    public AdminRoomResponseDTO create() {
        Room room = roomRepository.save(Room.create());
        AdminRoomResponseDTO response = roomMapper.toResponseDTO(room);

        log.info(
                "Room created by admin: roomId={}, roomVersion={}",
                room.getId(),
                room.getVersion()
        );

        afterCommitExecutor.run(chatMetricService::markRoomCreated);
        return response;
    }
}
