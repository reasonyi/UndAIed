package com.ssafy.undaied.socket.stage.dto.response;

import com.ssafy.undaied.socket.stage.constant.StageType;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class StageNotifyDto {
    private final Integer number = 0;
    private String content;

    public static StageNotifyDto notifyStartStage(StageType stage) {
        String message = stage == StageType.DAY || stage == StageType.NIGHT ?
                stage.getValue() + stage.getParticle() + " 되었습니다." :
                stage.getValue() + stage.getParticle() + " 시작합니다.";

        return StageNotifyDto.builder()
                .content(message)
                .build();
    }

    public static StageNotifyDto notifyEndStage(StageType stage) {
        String message = stage.getValue() + stage.getParticle() + " 종료합니다.";
        return StageNotifyDto.builder()
                .content(message)
                .build();
    }

}
