package com.chat.core.admin.message;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.chat.core.admin.member.AdminMemberMapper;
import com.chat.core.admin.room.AdminRoomMapper;
import com.chat.model.*;
import com.chat.persistence.message.Message;

@Mapper(
        componentModel = "spring",
        uses = { AdminMemberMapper.class, AdminRoomMapper.class }
)
public interface AdminMessageMapper {
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "parentId", source = "parent.id")
    MessageResponseDTO toResponseDTO(Message message);

    @Mapping(target = "parentId", source = "parent.id")
    AdminMessageExtendedDTO toExtendedResponseDTO(Message message);

    @Mapping(target = "member", source = "member")
    @Mapping(target = "parent", source = "parent")
    AdminMessageRoomItemDTO toMessageItemDTO(Message message);

    @Mapping(target = "member", source = "member")
    AdminParentMessageDTO toParentMessageDTO(Message message);

    @Mapping(target = "parent", source = "parent")
    AdminMessageMemberItemDTO toMessageItemMemberDTO(Message message);
}
