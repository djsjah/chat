package com.chat.core.admin.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.core.admin.member.AdminMemberMapper;
import com.chat.core.admin.room.AdminRoomMapper;
import com.chat.model.AdminMessageExtendedDTO;
import com.chat.model.AdminMessageMemberSliceDTO;
import com.chat.model.AdminMessagePaginationDTO;
import com.chat.model.AdminMessageRoomSliceDTO;
import com.chat.persistence.member.Member;
import com.chat.persistence.member.repository.AdminMemberRepository;
import com.chat.persistence.message.Message;
import com.chat.persistence.message.repository.AdminMessageRepository;
import com.chat.persistence.room.Room;
import com.chat.persistence.room.repository.admin.AdminRoomRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminMessageService {
    private final AdminMessageRepository messageRepository;
    private final AdminMessageMapper messageMapper;
    private final AdminRoomRepository roomRepository;
    private final AdminRoomMapper roomMapper;
    private final AdminMemberRepository memberRepository;
    private final AdminMemberMapper memberMapper;

    @Transactional(readOnly = true)
    public AdminMessageExtendedDTO getOneById(Long id) {
        return messageMapper.toExtendedResponseDTO(messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested message not found"))
        );
    }

    @Transactional(readOnly = true)
    public AdminMessageMemberSliceDTO getMessagesByMemberId(Long memberId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested member not found"));

        Slice<Message> slice = messageRepository.findSliceByMemberId(memberId, pageable);
        return new AdminMessageMemberSliceDTO(
                memberMapper.toResponseDTO(member),
                slice.getContent().stream()
                        .map(messageMapper::toMessageItemMemberDTO)
                        .toList(),
                new AdminMessagePaginationDTO(
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
    public AdminMessageRoomSliceDTO getMessagesByRoomId(Long roomId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Requested room not found"
                ));

        Slice<Message> slice = messageRepository.findSliceByRoomId(roomId, pageable);
        return new AdminMessageRoomSliceDTO(
                roomMapper.toResponseDTO(room),
                slice.getContent().stream()
                        .map(messageMapper::toMessageItemDTO)
                        .toList(),
                new AdminMessagePaginationDTO(
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
}
