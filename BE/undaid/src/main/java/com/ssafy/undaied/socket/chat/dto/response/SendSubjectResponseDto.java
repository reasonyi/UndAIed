package com.ssafy.undaied.socket.chat.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class SendSubjectResponseDto {
    private Integer number;
    private String content;

}
