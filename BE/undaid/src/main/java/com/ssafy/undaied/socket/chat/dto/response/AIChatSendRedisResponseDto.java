package com.ssafy.undaied.socket.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIChatSendRedisResponseDto {
    private AIChatAIListResponseDto[] selectedAIs;
    private String message;

    @Builder
    public AIChatSendRedisResponseDto(AIChatAIListResponseDto[] selectedAIs, String message) {
        this.selectedAIs = selectedAIs;
        this.message = message;
    }

}
