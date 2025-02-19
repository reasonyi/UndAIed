package com.ssafy.undaied.socket.init.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameInfoResponseDto {
    private Integer round;
    private String stage;
    private Integer gameId;
    private Integer timer;  // Added timer field
    private List<PlayerInfoDto> players;
}
