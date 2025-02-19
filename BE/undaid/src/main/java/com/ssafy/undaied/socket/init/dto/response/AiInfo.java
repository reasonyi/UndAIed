package com.ssafy.undaied.socket.init.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AiInfo {
    private int aiId;      // "1", "2", "3" 형태
    private int number;       // 할당된 번호
}
