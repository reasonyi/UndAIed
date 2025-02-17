package com.ssafy.undaied.socket.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRedisResponseDto {
    private String round;
    private String subject;
    private Map<String, String> data;

    @Builder
    public String sendData(String round, String subject, Map<String, String> data) {
        this.round = round;
        this.subject = subject;
        this.data = data;

        String Key1 = "subject_debate";
        String Key2 = "free_debate";
        String Key3 = "events";

        return String.format("<%s> [subject] %s [%s] %s [%s] %s [%s] %s",
                round, subject, Key1, data.get(Key1), Key2, data.get(Key2), Key3, data.get(Key3));
    }

}
