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

    FULL_USER_IN_ROOM(4022, "방에 인원이 다 찼습니다."),
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

    PLAYER_NOT_FOUND(4021, "플레이어를 찾을 수 없습니다."),

    // Game 관련 공통 예외 (4100-4199)
    GAME_NOT_STARTED(4101, "게임을 시작할 수 없습니다."),
    PLAYER_NOT_IN_GAME(4102, "게임에 참여하지 않은 사용자입니다."),
    GAME_ALREADY_START(4103, "이미 시작된 게임입니다."),

    // 투표 관련 예외 (4200-4299)
    VOTE_INVALID_TARGET(4201, "유효하지 않은 투표 대상입니다."),
    VOTE_INVALID_PLAYER(4202, "유효하지 않은 투표입니다."),
    VOTE_ALREADY_SUBMITTED(4203, "이미 투표를 제출했습니다."),
    VOTE_STAGE_INVALID(4204, "현재는 투표가 불가합니다."),
    VOTE_SUBMIT_FAILED(4205, "투표 제출이 실패하였습니다." ),
    VOTE_DIED_PLAYER(4206, "죽은 플레이어는 투표를 할 수 없습니다."),
    VOTE_SELF_TARGET(4208, "본인에게 투표할 수 없습니다."),

    // 감염 관련 예외
    INFECT_PLAYER_NOT_FOUND(4301, "감염이 가능한 플레이어가 없습니다."),
    INFECT_FAILED(4302, "감염 처리 중 오류가 발생했습니다."),

    //게임 종료 관련 에러
    GAME_STATUS_NOT_FOUND(4401, "게임 상태를 찾을 수 없습니다."),
    GAME_DATA_NOT_FOUND(4402, "게임 데이터를 찾을 수 없습니다."),
    CHECKING_GAME_ERROR(4403, "게임 결과 체크 중 알수 없는 에러가 발생했습니다."),

    NO_PLAYERS_FOUND(4404,"게임에서 플레이어를 찾을 수 없습니다."),
    GAME_END_PROCESS_FAILED(4405, "게임 종료 과정에서 에러가 발생했습니다."),
    GAME_UPDATE_FAILED(4406, "게임 상태 업데이트에 실패했습니다."),
    RESULT_CREATION_FAILED(4407, "게임 결과 생성에 실패했습니다."),
    CLIENT_NOT_FOUND(4408, "client를 찾을 수 없습니다."),
    ROOM_OPERATION_FAILED(4409, "방 나가기 처리 중 에러가 발생했습니다")
    ;

    private final int status;
    private final String message;
}
