package com.chat.app.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatEvent {
    USER_JOINED("user_joined"),
    USER_LEFT("user_left"),

    MESSAGE_ADDED("message_added"),
    MESSAGE_UPDATED("message_updated"),
    MESSAGE_DELETED("message_deleted");

    private final String value;

    @Override
    public String toString() { return value; }
}
