package com.ssafy.undaied.socket.room.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomLeaveRequestDto {
    private Long roomId;
}
