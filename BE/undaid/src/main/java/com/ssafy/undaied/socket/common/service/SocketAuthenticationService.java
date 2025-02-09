package com.ssafy.undaied.socket.common.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.global.auth.token.JwtTokenProvider;
import com.ssafy.undaied.global.common.exception.BaseException;
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

    /**
     * 클라이언트의 인증을 처리합니다.
     * @return 인증된 사용자의 ID
     */
    public int authenticateClient(SocketIOClient client) {
        String bearerToken = client.getHandshakeData().getHttpHeaders().get("Authorization");
        log.info("Raw Authorization header: {}", bearerToken);  // 추가
        log.debug("Received token: {}", bearerToken); // 추가됨
        String jwt = jwtTokenProvider.resolveToken(bearerToken);
        log.info("Extracted JWT: {}", jwt);  // 추가
        
        if(jwt == null) {
            log.error("No JWT token found in request"); // 추가됨
            throw new BaseException(NOT_AUTHENTICATED);
        }

        if (!jwtTokenProvider.validateToken(jwt)) {
            log.error("Invalid JWT token"); // 추가됨
            throw new BaseException(TOKEN_VALIDATION_FAILED);
        }
        int userId = jwtTokenProvider.getUserIdFromToken(jwt);// 추가됨
        log.debug("Successfully authenticated user: {}", userId); //추가됨됨
        return jwtTokenProvider.getUserIdFromToken(jwt);
    }
}