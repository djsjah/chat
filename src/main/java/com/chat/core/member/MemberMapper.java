package com.chat.core.member;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.chat.model.MemberResponseDTO;
import com.chat.persistence.member.Member;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    @Mapping(target = "isDeleted", source = "deleted")
    MemberResponseDTO toResponseDTO(Member member);
}
