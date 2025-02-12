package com.ssafy.undaied.socket.common.service;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.global.auth.token.JwtTokenProvider;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.exception.ErrorCode;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ssafy.undaied.global.common.exception.ErrorCode.NOT_AUTHENTICATED;
import static com.ssafy.undaied.global.common.exception.ErrorCode.TOKEN_VALIDATION_FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketAuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;

    public int authenticateClient(SocketIOClient client) {
        try {
            // 디버깅을 위한 handshake 데이터 출력
            HandshakeData handshakeData = client.getHandshakeData();
            Map<String, List<String>> params = handshakeData.getUrlParams();
            log.debug("Connection attempt - URL params: {}", params);

            // 먼저 auth 매개변수로 토큰을 찾음
            String token = null;
            if (params.containsKey("auth")) {
                token = params.get("auth").get(0);
                log.debug("Found token in URL params: {}", token);
            }

            //**임의로 수정 / 원래는 token = handshakeData.getSingleUrlParam("Authorization");
            // URL 매개변수에 없다면 헤더에서 찾기
            if (token == null) {
                token = handshakeData.getHttpHeaders().get("Authorization");
                log.debug("Found token in headers: {}", token);
            }

            if (token == null) {
                log.error("No authorization token found");
                throw new BaseException(ErrorCode.NOT_AUTHENTICATED);
            }

            // Bearer 접두사 처리
            if (!token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }

            String jwt = jwtTokenProvider.resolveToken(token);
            if (jwt == null) {
                log.error("Failed to resolve JWT token");
                throw new BaseException(ErrorCode.TOKEN_VALIDATION_FAILED);
            }

            if (!jwtTokenProvider.validateToken(jwt)) {
                log.error("JWT validation failed");
                throw new BaseException(ErrorCode.TOKEN_VALIDATION_FAILED);
            }

            int userId = jwtTokenProvider.getUserIdFromToken(jwt);
            log.debug("Successfully authenticated user ID: {}", userId);

            return userId;

        } catch (BaseException e) {
            log.error("Authentication failed with error code: {}", e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected authentication error", e);
            throw new BaseException(ErrorCode.NOT_AUTHENTICATED);
        }
    }
}