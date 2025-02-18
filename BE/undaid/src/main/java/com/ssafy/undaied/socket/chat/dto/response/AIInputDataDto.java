package com.ssafy.undaied.socket.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIInputDataDto {
    private AINumberDto[] selectedAIs;
    private String message;

    @Builder
    public AIInputDataDto(AINumberDto[] selectedAIs, String message) {
        this.selectedAIs = selectedAIs;
        this.message = message;
    }

}
