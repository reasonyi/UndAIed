package com.ssafy.undaied.socket.init.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameInfoResponseDto {
    private List<PlayerInfoDto> players;
}
