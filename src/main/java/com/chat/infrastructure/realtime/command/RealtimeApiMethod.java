package com.chat.infrastructure.realtime.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RealtimeApiMethod {
    PUBLISH("publish"),
    BROADCAST("broadcast");

    private final String value;

    public static RealtimeApiMethod fromValue(String value) {
        for (RealtimeApiMethod method : values()) {
            if (method.value.equals(value)) return method;
        }
        throw new IllegalArgumentException("Unknown RealtimeApiMethod: " + value);
    }

    @Override
    public String toString() { return value; }
}
