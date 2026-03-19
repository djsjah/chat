package com.chat.app.dto;

public record ApiErrorDTO(int status, String error, Object message) { }
