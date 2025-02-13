package com.ssafy.undaied.socket.lobby.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class LobbyRoomListResponseDto {

    private List<UpdateData> rooms;
    private Integer totalPage;

}
