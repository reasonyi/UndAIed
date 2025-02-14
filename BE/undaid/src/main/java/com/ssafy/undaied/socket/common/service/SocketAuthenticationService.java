package com.ssafy.undaied.socket.common.service;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.global.auth.token.JwtTokenProvider;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ssafy.undaied.global.common.exception.ErrorCode.NOT_AUTHENTICATED;
import static com.ssafy.undaied.global.common.exception.ErrorCode.TOKEN_VALIDATION_FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketAuthenticationService {

    private final JwtTokenProvider jwtTokenProvider;

    public int authenticateClient(SocketIOClient client) {
        try {
            // 1. Socket.io handshake data
            HandshakeData handshakeData = client.getHandshakeData();
            // 2. 쿼리 파라미터 "auth"에서 토큰 추출
            String token = handshakeData.getSingleUrlParam("auth");

            if (token == null) {
                log.error("auth 파라미터를 찾을 수 없습니다.");
                throw new BaseException(NOT_AUTHENTICATED);
            }

//            log.debug("Found auth param: {}", token);

            // 3. "Bearer " 접두사가 포함되어 있다면, JwtTokenProvider에서 제거
            //    (JwtTokenProvider.resolveToken()이 알아서 처리한다고 가정)
            String jwt = jwtTokenProvider.resolveToken(token);

            if (jwt == null) {
                log.error("jwt 토큰 추출에 실패했습니다.");
                throw new BaseException(TOKEN_VALIDATION_FAILED);
            }

            // 4. 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(jwt)) {
                log.error("jwt 인증에 실패했습니다.");
                throw new BaseException(TOKEN_VALIDATION_FAILED);
            }

            // 5. 토큰에서 userId 추출
            return jwtTokenProvider.getUserIdFromToken(jwt);

        } catch (BaseException e) {
            log.error("Authentication failed with error code: {}", e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected authentication error", e);
            throw new BaseException(NOT_AUTHENTICATED);
        }
    }
}