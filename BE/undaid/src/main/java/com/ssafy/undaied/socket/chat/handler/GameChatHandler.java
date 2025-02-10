package com.ssafy.undaied.socket.chat.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ssafy.undaied.socket.chat.dto.request.GameChatRequestDto;
import com.ssafy.undaied.socket.chat.service.GameChatService;
import com.ssafy.undaied.socket.stage.handler.GameStageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameChatHandler {

    private final GameChatService gameChatService;
    private final GameStageHandler gameStageHandler;

    @OnEvent("chat:game")
    public void GameChat(SocketIOClient client, GameChatRequestDto gameChatRequestDto) {
        Integer userId = client.get("userId");
        gameChatService.processGameChat(client, userId, gameChatRequestDto);
    }






}
