package com.ssafy.undaied.socket.json.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatDto {
    private Integer number;
    private String content;

    @Builder
    public ChatDto(Integer number, String content) {
        this.number = number;
        this.content = content;
    }
}
