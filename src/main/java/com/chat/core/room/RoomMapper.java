package com.chat.core.room;

import org.mapstruct.Mapper;

import com.chat.model.RoomResponseDTO;
import com.chat.persistence.room.Room;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    RoomResponseDTO toResponseDTO(Room room);
}
