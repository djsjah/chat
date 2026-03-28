package com.chat.persistence.cdc;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.chat.infrastructure.realtime.command.RealtimeApiMethod;

@Converter
public class RealtimeApiMethodConverter implements AttributeConverter<RealtimeApiMethod, String> {
    @Override
    public String convertToDatabaseColumn(RealtimeApiMethod attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RealtimeApiMethod convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RealtimeApiMethod.fromValue(dbData);
    }
}
