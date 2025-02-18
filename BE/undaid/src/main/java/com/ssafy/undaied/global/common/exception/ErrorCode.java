package com.ssafy.undaied.global.common.exception;

import com.ssafy.undaied.global.common.response.HttpStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Global Exception
    BAD_REQUEST_ERROR(HttpStatusCode.BAD_REQUEST.getStatus(), "잘못된 요청입니다."),
    INVALID_HTTP_MESSAGE_BODY(HttpStatusCode.BAD_REQUEST.getStatus(),"HTTP 요청 바디의 형식이 잘못되었습니다."),
    UNSUPPORTED_HTTP_METHOD(HttpStatusCode.METHOD_NOT_ALLOWED.getStatus(),"지원하지 않는 HTTP 메서드입니다."),
    SERVER_ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatus(),"서버 내부에서 알 수 없는 오류가 발생했습니다."),

    // Business Exception
    // 인증 예외
    NOT_AUTHENTICATED(HttpStatusCode.UNAUTHORIZED.getStatus(), "등록된 인증 정보가 없습니다."),

    // 회원 관리 예외
    USER_NOT_FOUND(HttpStatusCode.BAD_REQUEST.getStatus(), "유저를 찾을 수 없습니다."),
    DELETED_USER(HttpStatusCode.FORBIDDEN.getStatus(), "탈퇴한 회원입니다."),
    ALREADY_NICKNAME_EXISTS(HttpStatusCode.BAD_REQUEST.getStatus(), "이미 존재하는 닉네임입니다."),
    NOT_ALLOW_NICKNAME(HttpStatusCode.BAD_REQUEST.getStatus(), "사용할 수 없는 닉네임입니다."),
    NOT_ALLOW_SPECIAL_CHARACTERS(HttpStatusCode.BAD_REQUEST.getStatus(), "닉네임에 특수문자는 사용할 수 없습니다."),

    // 토큰 예외
    JWT_CREATION_FAILED(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatus(), "JWT 토큰 생성에 실패했습니다."),
    TOKEN_VALIDATION_FAILED(HttpStatusCode.BAD_REQUEST.getStatus(), "토큰 인증에 실패했습니다."),
    UNAUTHORIZED_TOKEN(HttpStatusCode.UNAUTHORIZED.getStatus(), "인증되지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatusCode.UNAUTHORIZED.getStatus(), "만료된 토큰입니다."),
    INVALID_USER_ID_FORMAT(HttpStatusCode.BAD_REQUEST.getStatus(), "토큰에서 userId 파싱에 실패했습니다."),

    // 게임 예외
    GAME_NOT_FOUND(HttpStatusCode.BAD_REQUEST.getStatus(), "게임을 찾을 수 없습니다."),

    // Board Exception
    // 게시글 예외
    BOARD_NOT_FOUND(HttpStatusCode.NOT_FOUND.getStatus(), "게시글을 찾을 수 없습니다."),
    BOARD_NOT_AUTHORIZED(HttpStatusCode.FORBIDDEN.getStatus(), "해당 게시글에 대한 수정/삭제 권한이 없습니다."),

    // 댓글 예외
    COMMENT_NOT_FOUND(HttpStatusCode.NOT_FOUND.getStatus(), "댓글을 찾을 수 없습니다."),
    COMMENT_NOT_AUTHORIZED(HttpStatusCode.FORBIDDEN.getStatus(), "해당 댓글에 대한 수정/삭제 권한이 없습니다."),

    // 친구 예외
    FRIENDSHIP_ALREADY_REQUESTED(HttpStatusCode.BAD_REQUEST.getStatus(), "이미 친구이거나 친구 요청을 보냈습니다."),

    SOCKET_CONNECTION_FAILED(HttpStatusCode.BAD_REQUEST.getStatus(), "소켓 연결에 실패했습니다."),
    SOCKET_EVENT_ERROR(HttpStatusCode.BAD_REQUEST.getStatus(), "소켓 이벤트 처리 중 오류가 발생했습니다."),
    SOCKET_DISCONNECTION_ERROR(HttpStatusCode.BAD_REQUEST.getStatus(), "소켓 연결 해제 중 제대로 처리되지 않은 작업이 있습니다."),
    ;

    private final int status;
    private final String message;
}
