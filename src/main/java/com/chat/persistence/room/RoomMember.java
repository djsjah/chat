package com.chat.persistence.room;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

import com.chat.persistence.member.Member;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_room_member",
                columnNames = { "room_id", "member_id" }
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "room_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_member_room")
    )
    private Room room;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_member_member")
    )
    private Member member;

    public RoomMember(Room room, Member member) {
        this.room = Objects.requireNonNull(room);
        this.member = Objects.requireNonNull(member);
    }
}
