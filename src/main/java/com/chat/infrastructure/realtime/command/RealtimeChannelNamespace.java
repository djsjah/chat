package com.chat.infrastructure.realtime.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RealtimeChannelNamespace {
    ROOM("room");

    private final String value;

    @Override
    public String toString() { return value; }
}
