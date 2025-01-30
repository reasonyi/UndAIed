package com.ssafy.undaid.global.common.exception;

import com.ssafy.undaid.global.common.response.HttpStatusCode;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Global Exception
    BAD_REQUEST_ERROR(HttpStatusCode.BAD_REQUEST.getStatus(), "잘못된 요청입니다."),
    INVALID_HTTP_MESSAGE_BODY(HttpStatusCode.BAD_REQUEST.getStatus(),"HTTP 요청 바디의 형식이 잘못되었습니다."),
    UNSUPPORTED_HTTP_METHOD(HttpStatusCode.METHOD_NOT_ALLOWED.getStatus(),"지원하지 않는 HTTP 메서드입니다."),
    SERVER_ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatus(),"서버 내부에서 알 수 없는 오류가 발생했습니다."),

    // Business Exception
    NOT_AUTHENTICATED(HttpStatusCode.UNAUTHORIZED.getStatus(), "등록된 인증 정보가 없습니다."),

    USER_NOT_FOUND(HttpStatusCode.BAD_REQUEST.getStatus(), "유저를 찾을 수 없습니다."),
    DELETED_USER(HttpStatusCode.FORBIDDEN.getStatus(), "탈퇴한 회원입니다."),


    JWT_CREATION_FAILED(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatus(), "JWT 토큰 생성에 실패했습니다."),
    TOKEN_VALIDATION_FAILED(HttpStatusCode.BAD_REQUEST.getStatus(), "토큰 인증에 실패했습니다."),
    UNAUTHORIZED_TOKEN(HttpStatusCode.UNAUTHORIZED.getStatus(), "인증되지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatusCode.UNAUTHORIZED.getStatus(), "만료된 토큰입니다."),
    INVALID_USER_ID_FORMAT(HttpStatusCode.BAD_REQUEST.getStatus(), "토큰에서 userId 파싱에 실패했습니다."),

    GAME_NOT_FOUND(HttpStatusCode.BAD_REQUEST.getStatus(), "게임을 찾을 수 없습니다.")
    ;

    private final int httpStatus;
    private final String message;
}
