package com.ssafy.undaied.socket.common.constant;

public enum EventType {

    // 로비
    CREATE_ROOM_AT_LOBBY("lobby:room:create"),
    UPDATE_LOBBY("lobby:room:update"),
    CREATE_ROOM("room:create"),
    LIST_ROOMS("room:list"),
    ENTER_ROOM_AT_LOBBY("lobby:room:enter"),
    ENTER_ROOM("room:enter"),
    LOOBY_CHAT("lobby:chat"),

    // 대기방
    LEAVE_ROOM("room:leave"),
    UPDATE_ROOM_HOST("room:host:update"),
    ROOM_CHAT("room:chat"),
    START_GAME("game:start"),

    GAME_INFO("game:info"),

    // 게임진행(낮)
    SEND_SUBJECT("send:subject"),
    GAME_CHAT("chat:game"),

    // 투표
    SUBMIT_VOTE ("vote:submit"),
    SHOW_VOTE_RESULT("vote:result"),

    // 게임 진행 밤
    SHOW_INFECTION_RESULT("infection:result"),

    //게임 중 나가기

    QUIT_GAME("quit:game"),

    // 게임 종료
    SHOW_GAME_RESULT("game:result")

    ;

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}