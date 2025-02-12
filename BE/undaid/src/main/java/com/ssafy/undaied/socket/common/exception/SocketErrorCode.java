package com.ssafy.undaied.socket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocketErrorCode {

    // socketIo 예외
    SOCKET_CONNECTION_FAILED(4000, "소켓 연결에 실패했습니다."),
    SOCKET_AUTHENTICATION_FAILED(4001, "소켓 인증에 실패했습니다."),
    SOCKET_DISCONNECTED(4002, "소켓 연결이 종료되었습니다."),
    SOCKET_EVENT_ERROR(4003, "소켓 이벤트 처리 중 오류가 발생했습니다."),

    SOCKET_MESSAGE_FAILED(4005, "메시지 전송에 실패했습니다."),

    USER_INFO_NOT_FOUND(4012, "유저 정보를 찾을 수 없습니다."),

    SOCKET_ROOM_JOIN_FAILED(4004, "게임방 참여에 실패했습니다."),
    CREATE_ROOM_FAILED(4006, "방 생성에 실패했습니다."),
    USER_ALREADY_IN_ROOM(4007, "이미 방에 있는 유저입니다."),
    ROOM_NOT_FOUND(4008, "방을 찾을 수 없습니다."),
    USER_NOT_IN_ROOM(4009, "유저가 나가려는 방에 없습니다."),
    LEAVE_ROOM_FAILED(4010,"방을 나갈 수 없습니다."),
    INVALID_ROOM_PASSWORD(4011, "비밀번호가 일치하지 않습니다."),

    ROOM_CHAT_FAILED(4012, "채팅 전송에 실패했습니다."),

    LOBBY_CHAT_FAILED(4013, "로비 채팅 전송에 실패했습니다."),

    GAME_ALREADY_INITIALIZING(4014, "게임 초기화가 이미 진행 중입니다."),

    NOT_ROOM_HOST(4015, "방장만 게임을 시작할 수 있습니다."),
    INVALID_PLAYER_COUNT(4016, "참가자 수가 올바르지 않습니다."),

    SOCKET_DISCONNECTION_ERROR(4017, "소켓 연결 종료 중 오류가 발생했습니다."),
    NOT_HOST(4018,"방장만 게임을 진행할 수 있습니다."),
    GAME_NOT_FOUND(4019, "게임을 찾을 수 없습니다."),

    GAME_NOT_ENDED(4020, "게임이 정상적으로 끝나지 않았습니다."),

    PLAYER_NOT_FOUND(4021, "플레이어를 찾을 수 없습니다.")


    ;

    private final int status;
    private final String message;
}
