package com.chat.cdc.dto;

import com.fasterxml.jackson.databind.JsonNode;

import com.chat.infrastructure.realtime.command.RealtimeApiMethod;

public record CdcCreateDTO(long partition, RealtimeApiMethod method, JsonNode payload) { }
