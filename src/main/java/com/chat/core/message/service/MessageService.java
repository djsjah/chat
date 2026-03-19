package com.chat.core.message.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.core.message.MessageMapper;
import com.chat.core.room.RoomMapper;
import com.chat.model.MessagePaginationDTO;
import com.chat.model.MessageResponseDTO;
import com.chat.model.MessageRoomSliceDTO;
import com.chat.persistence.message.Message;
import com.chat.persistence.message.repository.MessageRepository;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.RoomMemberRepository;
import com.chat.persistence.room.repository.RoomRepository;
import com.chat.security.CurrentMemberProvider;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MessageService {
    private final CurrentMemberProvider currentMember;

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional(readOnly = true)
    public MessageRoomSliceDTO getMessagesByRoomId(Long roomId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (!roomMemberRepository.existsByRoomIdAndMemberSubject(roomId, currentMember.subject())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Requested room not found"
                ));

        Slice<Message> slice = messageRepository.findSliceByRoomId(roomId, pageable);
        return new MessageRoomSliceDTO(
                roomMapper.toResponseDTO(room),
                slice.getContent().stream()
                        .map(messageMapper::toMessageItemDTO)
                        .toList(),
                new MessagePaginationDTO(
                        slice.getNumber(),
                        slice.getSize(),
                        slice.getNumberOfElements(),
                        slice.isFirst(),
                        slice.isLast(),
                        slice.hasNext(),
                        slice.hasPrevious()
                )
        );
    }

    @Transactional(readOnly = true)
    public MessageResponseDTO getOneById(Long messageId) {
        return messageMapper.toResponseDTO(messageRepository
                .findAccessibleById(messageId, currentMember.subject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested message not found"))
        );
    }
}
