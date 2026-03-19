package com.chat.persistence.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.persistence.member.Member;

public interface AdminMemberRepository extends JpaRepository<Member, Long> { }
