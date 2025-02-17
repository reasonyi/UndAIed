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
    GAME_INIT_EMIT("game:init:emit"),
    GAME_INIT_SEND("game:init:send"),
    ROOM_CHAT_EMIT("room:chat:emit"),
    ROOM_CHAT_SEND("room:chat:send"),

    // 게임진행(낮)
    CHAT_SUBJECT_EMIT("chat:subject:emit"),
    CHAT_SUBJECT_SEND("chat:subject:send"),

    START_GAME("game:start:emit"),
    GAME_INFO_EMIT("game:info:emit"),
    GAME_INFO_SEND("game:info:send"),
    GAME_CHAT_SEND("game:chat:send"),
    GAME_CHAT_EMIT("game:chat:emit"),

    GAME_RESULT_SEND("game:result:send"),

    // 투표
    SUBMIT_VOTE("vote:submit:emit"),
    SHOW_VOTE_RESULT("vote:result:send"),

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