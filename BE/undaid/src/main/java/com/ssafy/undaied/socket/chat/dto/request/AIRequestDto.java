package com.ssafy.undaied.socket.chat.dto.request;


import com.ssafy.undaied.socket.json.dto.JsonRoundInfoDto;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AIRequestDto {
    private Integer ai_num;
    private Integer ai_assist;
    private Map<Integer, JsonRoundInfoDto> message;

}
