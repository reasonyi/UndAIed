package com.ssafy.undaied.socket.room.dto.response;

import com.ssafy.undaied.socket.room.dto.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RoomEnterResponseDto {

    private Integer enterId;
    private RoomCreateResponseDto room;

}
