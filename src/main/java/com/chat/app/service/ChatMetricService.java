package com.chat.app.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMetricService {
    private final MeterRegistry meterRegistry;

    private Counter messagesCreated;
    private Counter messagesUpdated;
    private Counter messagesDeleted;
    private Counter membersCreated;
    private Counter membersDeleted;
    private Counter membersJoined;
    private Counter membersLeft;
    private Counter roomsCreated;

    @PostConstruct
    void init() {
        messagesCreated = Counter.builder("chat_messages_created_total")
                .description("Total number of created chat messages")
                .register(meterRegistry);

        messagesUpdated = Counter.builder("chat_messages_updated_total")
                .description("Total number of updated chat messages")
                .register(meterRegistry);

        messagesDeleted = Counter.builder("chat_messages_deleted_total")
                .description("Total number of deleted chat messages")
                .register(meterRegistry);

        membersCreated = Counter.builder("chat_members_created_total")
                .description("Total number of created chat members")
                .register(meterRegistry);

        membersDeleted = Counter.builder("chat_members_deleted_total")
                .description("Total number of deleted chat members")
                .register(meterRegistry);

        membersJoined = Counter.builder("chat_members_joined_total")
                .description("Total number of room join events")
                .register(meterRegistry);

        membersLeft = Counter.builder("chat_members_left_total")
                .description("Total number of room leave events")
                .register(meterRegistry);

        roomsCreated = Counter.builder("chat_rooms_created_total")
                .description("Total number of created chat rooms")
                .register(meterRegistry);
    }

    public void markMessageCreated() { messagesCreated.increment(); }
    public void markMessageUpdated() { messagesUpdated.increment(); }
    public void markMessageDeleted() { messagesDeleted.increment(); }
    public void markMemberCreated() { membersCreated.increment(); }
    public void markMemberDeleted() { membersDeleted.increment(); }
    public void markMemberJoined() { membersJoined.increment(); }
    public void markMemberLeft() { membersLeft.increment(); }
    public void markRoomCreated() { roomsCreated.increment(); }
}
