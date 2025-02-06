package com.ssafy.undaied.socket.common.constant;

public enum EventType {

    // 로비
    CREATE_ROOM_AT_LOBBY("lobby:room:create"),
    CREATE_ROOM("room:create"),
    LIST_ROOMS("room:list"),
    ENTER_ROOM("room:enter"),
    SEND_LOBBY_CHAT("chat:lobby:send"),

    // 대기방
    LEAVE_ROOM("room:leave"),
    UPDATE_ROOM_HOST("room:host:update"),
    SEND_ROOM_CHAT("chat:room:send"),
    START_GAME("game:start"),

    // 게임진행(낮)
    START_SUBJECT_DEBATE("debate:subject:start"),
    SEND_SUBJECT_DEBATE("debate:subject:send"),
    END_SUBJECT_DEBATE("debate:subject:end"),
    START_FREE_DEBATE("debate:free:start"),
    SEND_FREE_DEBATE("debate:free:send"),
    END_FREE_DEBATE("debate:free:end"),

    // 투표
    START_VOTE("vote:start"),
    END_VOTE("vote:end"),
    SUBMIT_VOTE ("vote:submit"),
    SHOW_VOTE_RESULT("vote:result"),

    // 게임 진행 밤
    SHOW_INFECTION_RESULT("infection:result"),

    // 스테이지 알림
    START_STAGE("game:stage:start"),
    END_STAGE("game:stage:end"),

    // 게임 종료
    SHOW_GAME_RESULT("game:result"),
    RETURN_TO_ROOM("room:return")
    ;

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}