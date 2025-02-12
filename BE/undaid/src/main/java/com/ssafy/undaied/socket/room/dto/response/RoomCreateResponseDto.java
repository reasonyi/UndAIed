package com.ssafy.undaied.socket.room.dto.response;

import com.ssafy.undaied.socket.room.dto.RoomUser;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class RoomCreateResponseDto {

    private Long roomId;
    private String roomTitle;
    private Boolean isPrivate;
    private Boolean playing;
    private List<RoomUserResponseDto> currentPlayers;

}
