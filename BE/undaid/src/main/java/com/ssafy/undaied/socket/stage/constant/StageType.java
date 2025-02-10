package com.ssafy.undaied.socket.stage.constant;

public enum StageType {
    START("게임", "을"),
    DAY("낮", "이"),
    NIGHT("밤", "이"),
    FREE_DEBATE("자유 토론", "을"),
    SUBJECT_DEBATE("주제 토론", "을"),
    VOTE("투표", "를"),
    INFECTION("감염", "이"),
    FINISH("게임", "을")
    ;

    private final String value;
    private final String particle;

    StageType(String value, String particle){
        this.value = value;
        this.particle = particle;
    }

    public String getValue() {
        return value;
    }

    public String getParticle() {
        return particle;
    }
}
