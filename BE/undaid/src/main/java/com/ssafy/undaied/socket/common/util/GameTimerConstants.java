package com.ssafy.undaied.socket.common.util;

public class GameTimerConstants {
    // 게임 전체 단계
    public static final int GAME_START = 1;
    public static final int GAME_END = 2;

    // 스테이지 알림
    public static final int STAGE_START_NOTIFY = 3;  // 각 스테이지 시작 알림
    public static final int STAGE_END_NOTIFY = 4;    // 각 스테이지 종료 알림

    // 스테이지 진행
    public static final int STAGE_MAIN = 5;          // 주요 진행 시간 (토론, 투표 등)

    // 특별 이벤트
    public static final int EVENT_NOTIFY = 6;        // 감염, 투표 결과 등 특별 이벤트 알림
    public static final int EVENT_RESULT = 7;        // 투표 결과, 토론 결과 등 결과 알림
}
