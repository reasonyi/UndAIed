package com.ssafy.undaied.socket.stage.constant;

public enum StageType {
    START("게임", "을", "start"),
    DAY("낮", "이", "day"),
    NIGHT("밤", "이", "night"),
    FREE_DEBATE("자유 토론", "을", "free_debate"),
    SUBJECT_DEBATE("주제 토론", "을", "subject_debate"),
    VOTE("투표", "를", "vote"),
    INFECTION("감염", "이", "infection"),
    FINISH("게임", "을", "finish")
    ;

    private final String value;
    private final String particle;
    private final String redisValue;

    StageType(String value, String particle, String redisValue){
        this.value = value;
        this.particle = particle;
        this.redisValue = redisValue;
    }

    public String getValue() {
        return value;
    }

    public String getParticle() {
        return particle;
    }

    public String getRedisValue() { return redisValue; }
}
