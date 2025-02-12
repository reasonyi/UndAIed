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
            HandshakeData handshakeData = client.getHandshakeData();
            Map<String, List<String>> authData = handshakeData.getUrlParams();

            // auth 파라미터에서 토큰 가져오기
            if (!authData.containsKey("token")) {
                log.error("No token found in auth data");
                throw new BaseException(NOT_AUTHENTICATED);
            }

            String token = authData.get("token").get(0);
            log.debug("Found token in auth data: {}", token);

            // 이미 Bearer prefix가 포함되어 있으므로 추가할 필요 없음
            String jwt = jwtTokenProvider.resolveToken(token);
            if (jwt == null) {
                log.error("Failed to resolve JWT token");
                throw new BaseException(TOKEN_VALIDATION_FAILED);
            }

            if (!jwtTokenProvider.validateToken(jwt)) {
                log.error("JWT validation failed");
                throw new BaseException(TOKEN_VALIDATION_FAILED);
            }

            int userId = jwtTokenProvider.getUserIdFromToken(jwt);
            log.debug("Successfully authenticated user ID: {}", userId);

            return userId;

        } catch (BaseException e) {
            log.error("Authentication failed with error code: {}", e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected authentication error", e);
            throw new BaseException(NOT_AUTHENTICATED);
        }
    }
}