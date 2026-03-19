package com.chat.persistence.room;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

import com.chat.persistence.message.Message;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "last_message_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_room_message")
    )
    private Message lastMessage;

    @Column(nullable = false)
    private long version = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime bumpedAt;

    public static Room create() {
        return new Room();
    }

    public void applyNewMessage(Message message, OffsetDateTime now) {
        this.lastMessage = message;
        this.bumpedAt = now;
        this.version++;
    }
}
