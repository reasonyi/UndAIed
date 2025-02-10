package com.ssafy.undaied.socket.lobby.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class LobbyUpdateResponseDto {
    private String type;
    private UpdateData data;
}
