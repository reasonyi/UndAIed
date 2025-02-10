package com.ssafy.undaied.socket.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RoomChatResponseDto {
    private String nickname;
    private String message;
}
