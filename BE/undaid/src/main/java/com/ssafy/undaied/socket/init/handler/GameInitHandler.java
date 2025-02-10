package com.ssafy.undaied.socket.init.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.init.dto.request.GameInitRequestDto;
import com.ssafy.undaied.socket.init.service.GameInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameInitHandler {
    private final GameInitService gameInitService;

    @OnEvent("game:init")
    public void handleGameInit(SocketIOClient client, GameInitRequestDto requestDto) {
        try {
            log.info("Game initialization requested - roomId: {}", requestDto.getRoomId());
            gameInitService.startGame(client, requestDto.getRoomId());
        } catch (SocketException e) {
            log.error("Failed to initialize game: {}", e.getMessage());
        }
    }
}
