package com.chat.core.message;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.chat.core.member.MemberMapper;
import com.chat.core.room.RoomMapper;
import com.chat.model.MessageItemDTO;
import com.chat.model.MessageResponseDTO;
import com.chat.model.MessageWithRoomDTO;
import com.chat.model.ParentMessageDTO;
import com.chat.persistence.message.Message;
import com.chat.persistence.room.Room;

@Mapper(
        componentModel = "spring",
        uses = { MemberMapper.class, RoomMapper.class }
)
public interface MessageMapper {
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "parentId", source = "parent.id")
    MessageResponseDTO toResponseDTO(Message message);

    @Mapping(target = "id", source = "message.id")
    @Mapping(target = "parentId", source = "message.parent.id")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "createdAt", source = "message.createdAt")
    @Mapping(target = "updatedAt", source = "message.updatedAt")
    @Mapping(target = "room", source = "room")
    MessageWithRoomDTO toResponseWithRoomDTO(Message message, Room room);

    @Mapping(target = "member", source = "member")
    @Mapping(target = "parent", source = "parent")
    MessageItemDTO toMessageItemDTO(Message message);

    @Mapping(target = "member", source = "member")
    ParentMessageDTO toParentMessageDTO(Message message);
}
