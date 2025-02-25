package com.ssafy.undaied.socket.quit.handler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.quit.dto.request.QuitRequestDto;
import com.ssafy.undaied.socket.quit.service.QuitService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


//일단 기능에서 제외
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class QuitHandler {
//    private final QuitService quitService;
//    private final SocketIONamespace namespace;
//
//    @PostConstruct
//    private void init() {
//        namespace.addEventListener("quit:game", QuitRequestDto.class,
//                (client, requestDto, ackRequest) -> {
//                    try {
////                        // 🔹 클라이언트 정보 가져오기 -- 나중에 살려야
////                        Integer gameId = client.get("gameId");
////                        if (gameId == null) {
////                            throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
////                        }
//
//                        // 🔹 WebSocket 연결 시 URL 파라미터에서 userId & gameId 가져오기 --일단 테스트 용
//                        String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
//                        String gameIdStr = client.getHandshakeData().getSingleUrlParam("gameId");
//
//                        if (userIdStr == null || gameIdStr == null) {
//                            throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
//                        }
//
//                        Integer userId = Integer.parseInt(userIdStr);
//                        Integer gameId = Integer.parseInt(gameIdStr);
//
//                        log.info("플레이어가 게임을 나갔습니다. - gameId: {}", gameId);
//
//                        // 🔹 퇴장 처리
//                        boolean success = quitService.leaveGame(client, requestDto.isInGame());
//
//                        // 🔹 클라이언트에게 응답 반환
//                        if (ackRequest.isAckRequested()) {
//                            Map<String, Object> response = new HashMap<>();
//                            response.put("success", success);
//                            response.put("errorMessage", null);
//                            response.put("gameId", gameId);
//                            ackRequest.sendAckData(response);
//                        }
//
//                    } catch (SocketException e) {
//                        log.error("Player quit failed: {}", e.getMessage());
//
//                        if (ackRequest.isAckRequested()) {
//                            Map<String, Object> response = new HashMap<>();
//                            response.put("success", false);
//                            response.put("errorMessage", e.getMessage());
//                            response.put("gameId", null);
//                            ackRequest.sendAckData(response);
//                        }
//                    }
//                });
//    }
//}
