package com.chat.core.admin.room;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.chat.core.admin.member.AdminMemberMapper;
import com.chat.model.AdminRoomResponseDTO;
import com.chat.model.AdminRoomWithMemberDTO;
import com.chat.persistence.member.Member;
import com.chat.persistence.room.Room;

@Mapper(componentModel = "spring", uses = AdminMemberMapper.class)
public interface AdminRoomMapper {
    AdminRoomResponseDTO toResponseDTO(Room room);

    @Mapping(target = "id", source = "room.id")
    @Mapping(target = "version", source = "room.version")
    @Mapping(target = "createdAt", source = "room.createdAt")
    @Mapping(target = "bumpedAt", source = "room.bumpedAt")
    @Mapping(target = "member", source = "member")
    AdminRoomWithMemberDTO toResponseWithMemberDTO(Room room, Member member);
}
