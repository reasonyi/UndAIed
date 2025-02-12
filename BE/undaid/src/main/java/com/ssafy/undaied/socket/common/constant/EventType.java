package com.ssafy.undaied.socket.common.constant;

public enum EventType {

    // 로비
    ROOM_LIST_AT_LOBBY("lobby:room:list"),
    LOBBY_CHAT("lobby:chat"),
    CREATE_ROOM_AT_LOBBY("lobby:room:create"),
    UPDATE_ROOM_AT_LOBBY("lobby:room:update"),

    // 대기방
    ENTER_ROOM_EMIT("room:enter:emit"),
    ENTER_ROOM_SEND("room:enter:send"),
    LEAVE_ROOM_EMIT("room:leave:emit"),
    LEAVE_ROOM_SEND("room:leave:send"),
    ROOM_CHAT_EMIT("room:chat:emit"),
    ROOM_CHAT_SEND("room:chat:send"),
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