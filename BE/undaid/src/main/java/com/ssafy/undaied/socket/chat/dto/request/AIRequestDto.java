package com.ssafy.undaied.socket.chat.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIRequestDto {
    private Integer ai_num;
    private Integer ai_assist;
    private String message;

}
