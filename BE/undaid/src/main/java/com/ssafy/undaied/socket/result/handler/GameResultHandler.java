package com.ssafy.undaied.socket.result.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.result.service.GameResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameResultHandler {
    private final GameResultService gameResultService;

    @OnEvent("game:result")
    public void handleGameResult(SocketIOClient client) {
        try {
            Integer gameId = client.get("gameId");
            if (gameId == null) {
                throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
            }

            log.info("Game result check requested - gameId: {}", gameId);
            gameResultService.checkGameResult(client, gameId);

        } catch (SocketException e) {
            log.error("Failed to check game result: {}", e.getMessage());
        }
    }
}