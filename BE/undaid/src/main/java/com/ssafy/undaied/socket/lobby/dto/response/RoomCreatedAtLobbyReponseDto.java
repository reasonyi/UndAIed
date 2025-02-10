package com.ssafy.undaied.socket.lobby.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class RoomCreatedAtLobbyReponseDto {
    private Long roomId;
    private String roomTitle;
    private Boolean isPrivate;
    private int currentPlayerNum;
    private Boolean playing;
}
