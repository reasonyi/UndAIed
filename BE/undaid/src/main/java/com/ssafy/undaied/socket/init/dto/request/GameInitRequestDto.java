package com.ssafy.undaied.socket.init.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class GameInitRequestDto {
    private int roomId;
}
