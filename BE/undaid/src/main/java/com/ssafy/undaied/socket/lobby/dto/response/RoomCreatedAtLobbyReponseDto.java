package com.ssafy.undaied.socket.lobby.dto.response;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class RoomCreatedAtLobbyReponseDto {
    private Long roomId;
    private String roomTitle;
    private Boolean isPrivate;
    private int currentPlayerNum;
}
