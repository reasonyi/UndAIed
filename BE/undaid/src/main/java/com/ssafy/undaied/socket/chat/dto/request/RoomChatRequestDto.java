package com.ssafy.undaied.socket.chat.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomChatRequestDto {
    private Long roomId;
    private String message;
}
