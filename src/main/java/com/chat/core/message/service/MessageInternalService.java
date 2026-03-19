package com.chat.core.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.model.MessageCreateDTO;
import com.chat.model.MessagePatchDTO;
import com.chat.persistence.message.Message;
import com.chat.persistence.message.repository.MessageRepository;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.RoomMember;
import com.chat.persistence.room.repository.RoomMemberRepository;

@Service
@RequiredArgsConstructor
public class MessageInternalService {
    private final MessageRepository messageRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Message getLatestByRoomId(Long roomId) {
        return messageRepository.findLatestByRoomId(roomId).orElse(null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Message create(String memberSubject, Room room, MessageCreateDTO createDTO) {
        RoomMember roomMember = roomMemberRepository.findWithMemberByRoomIdAndMemberSubject(room.getId(), memberSubject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested room not found"));

        Message.MessageBuilder messageBuilder = Message.builder()
                .content(createDTO.getContent())
                .room(room)
                .member(roomMember.getMember());

        if (createDTO.getParentId() != null) {
            messageBuilder.parent(
                    messageRepository.findByIdAndRoomId(createDTO.getParentId(), room.getId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Parent message must be in the same room"
                            ))
            );
        }

        return messageRepository.save(messageBuilder.build());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Message patchOneById(Long id, String memberSubject, MessagePatchDTO patchDTO) {
        Message message = messageRepository.findByIdAndMemberSubject(id, memberSubject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested message not found"));

        message.setContent(patchDTO.getContent());
        return message;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Message deleteOneById(Long id, String memberSubject) {
        Message message = messageRepository.findByIdAndMemberSubject(id, memberSubject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested message not found"));

        messageRepository.delete(message);
        return message;
    }
}
