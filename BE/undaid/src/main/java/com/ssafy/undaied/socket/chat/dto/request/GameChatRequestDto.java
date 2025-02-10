package com.ssafy.undaied.socket.chat.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class GameChatRequestDto {
    private String content;

}
