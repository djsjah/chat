package com.chat.core.admin.room.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.app.AfterCommitExecutor;
import com.chat.app.metric.ChatMetricService;
import com.chat.core.admin.room.AdminRoomMapper;
import com.chat.model.AdminRoomResponseDTO;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.admin.AdminRoomRepository;

@Service
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

        afterCommitExecutor.run(chatMetricService::markRoomCreated);
        return response;
    }
}
