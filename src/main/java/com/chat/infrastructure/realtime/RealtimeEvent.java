package com.chat.infrastructure.realtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import com.chat.app.event.ChatEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RealtimeEvent(ChatEvent type, JsonNode body) { }
