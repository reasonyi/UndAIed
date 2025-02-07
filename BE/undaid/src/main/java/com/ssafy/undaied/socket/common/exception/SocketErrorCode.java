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
    SOCKET_ROOM_JOIN_FAILED(4004, "게임방 참여에 실패했습니다."),
    SOCKET_MESSAGE_FAILED(4005, "메시지 전송에 실패했습니다."),
    CREATE_ROOM_FAILED(4006, "방 생성에 실패했습니다."),
    USER_ALREADY_IN_ROOM(4007, "이미 방에 있는 유저입니다.")

    ;


    private final int status;
    private final String message;
}
