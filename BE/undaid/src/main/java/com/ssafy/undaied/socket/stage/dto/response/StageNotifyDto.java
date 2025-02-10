package com.ssafy.undaied.socket.stage.dto.response;

import com.ssafy.undaied.socket.stage.constant.StageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Getter
@NoArgsConstructor
public class StageNotifyDto {
    private String stage;       // 낮/밤

    @Builder
    public StageNotifyDto(String stage) {
        this.stage = stage;
    }

    public String notifyStartStage(StageType stage) {
        if (stage == StageType.DAY || stage == StageType.NIGHT)
            return stage.getValue() + stage.getParticle() + " 되었습니다.";
        else return stage.getValue() + stage.getParticle() + " 시작합니다.";
    }

    public String notifyEndStage(StageType stage) {
        return stage.getValue() + stage.getParticle() + " 종료합니다.";
    }

}
