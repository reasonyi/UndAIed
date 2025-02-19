package com.ssafy.undaied.socket.init.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerInfoDto {
    private int number; // 익명 ID 인덱스 (1~7)
    private boolean isDied; // 생존 여부
    private boolean isInGame;
}
