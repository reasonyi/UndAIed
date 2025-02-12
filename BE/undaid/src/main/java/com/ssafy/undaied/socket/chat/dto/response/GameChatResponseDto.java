package com.ssafy.undaied.socket.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
public class GameChatResponseDto {
    private int number;
    private String content;

}
