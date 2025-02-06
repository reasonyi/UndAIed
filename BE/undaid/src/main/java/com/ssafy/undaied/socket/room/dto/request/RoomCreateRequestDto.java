package com.ssafy.undaied.socket.room.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequestDto {
    private String roomTitle;
    private Boolean isPrivate;
    private Integer roomPassword;
}
