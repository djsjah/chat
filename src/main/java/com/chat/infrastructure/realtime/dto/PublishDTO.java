package com.chat.infrastructure.realtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

import com.chat.infrastructure.realtime.RealtimeEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record PublishDTO(
        String channel,
        RealtimeEvent data,

        @JsonProperty("skip_history")
        Boolean skipHistory,

        Map<String, String> tags,
        String b64data,

        @JsonProperty("idempotency_key")
        String idempotencyKey,

        Boolean delta
) { }
