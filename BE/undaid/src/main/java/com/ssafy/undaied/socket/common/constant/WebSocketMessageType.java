package com.ssafy.undaied.socket.common.constant;

public enum WebSocketMessageType {

    //로비
    CREATE_ROOM,
    LIST_ROOMS,
    ENTER_ROOM,
    CHAT_LOBBY,

    //대기방

    LEAVE_ROOM,
    HOST_CHANGED,
    CHAT_ROOM,
    GAME_START,

    //게임진행(낮)
    SUBJECT_DEBATE_START,
    SUBJECT_DEBATE,
    SUBJECT_DEBATE_END,
    FREE_DEBATE_START,
    FREE_DEBATE,
    FREE_DEBATE_END,

    //투표
    VOTE_START,
    SUBMIT_VOTE,
    VOTE_RESULT,

    //게임 진행 밤
    INFECTION_RESULT,

    //낮/밤 알림
    TIME_CHANGE,

    //게임 종료
    GAME_RESULT,
    RETURN_TO_ROOM














}
