package com.chat.core.admin.member;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.chat.model.AdminMemberResponseDTO;
import com.chat.persistence.member.Member;

@Mapper(componentModel = "spring")
public interface AdminMemberMapper {
    @Mapping(target = "isDeleted", source = "deleted")
    AdminMemberResponseDTO toResponseDTO(Member member);
}
