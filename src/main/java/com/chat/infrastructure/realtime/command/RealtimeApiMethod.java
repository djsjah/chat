package com.chat.infrastructure.realtime.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RealtimeApiMethod {
    PUBLISH("publish"),
    BROADCAST("broadcast");

    private final String value;

    @Override
    public String toString() { return value; }
}
