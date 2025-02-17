//package com.ssafy.undaied.socket.result.handler;
//
//import com.corundumstudio.socketio.SocketIOClient;
//import com.corundumstudio.socketio.SocketIONamespace;
//import com.corundumstudio.socketio.SocketIOServer;
//import com.corundumstudio.socketio.annotation.OnEvent;
//import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
//import com.ssafy.undaied.socket.common.exception.SocketException;
//import com.ssafy.undaied.socket.result.dto.response.GameResultResponseDto;
//import com.ssafy.undaied.socket.result.service.GameResultService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class GameResultHandler {
//
//    private final GameResultService gameResultService;
//    private final RedisTemplate<String, String> redisTemplate;
//    private final SocketIONamespace namespace;
//
//    @PostConstruct
//    private void init() {
//        namespace.addEventListener("game:result", Integer.class,
//                (client, gameId, ackRequest) -> {
//                    try {
//
//                        // 1️⃣ URL 파라미터에서 gameId 가져오기, 임시 설정. 나중에 수정 필요
//                        if (gameId == null) {
//                            String gameIdStr = client.getHandshakeData().getSingleUrlParam("gameId");
//                            if (gameIdStr == null) {
//                                throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
//                            }
//                            gameId = Integer.parseInt(gameIdStr);
//                        }
//
//                        log.info("게임종료 여부 확인 - gameId: {}", gameId);
//
//                        // 🔹 게임 결과 확인
//                        GameResultResponseDto result = gameResultService.checkGameResult(client, gameId);
//
//                        // 🔹 게임 종료 후 게임방에서 나가고 원래 room으로 이동
//                        boolean movedToRoom = gameResultService.movePlayersToRoom(client, gameId);
//
//                        // 게임 결과 DB에 저장
//                        gameResultService.saveGameResult(gameId);
//
//                        // 🔹 클라이언트에게 응답 반환
//                        if (ackRequest.isAckRequested()) {
//                            Map<String, Object> response = new HashMap<>();
//                            response.put("success", true);
//                            response.put("errorMessage", null);
//                            response.put("data", result);
//                            response.put("movedToRoom", movedToRoom);
//                            ackRequest.sendAckData(response);
//                        }
//
//                    } catch (SocketException e) {
//                        log.error("Failed to check game result: {}", e.getMessage());
//
//                        if (ackRequest.isAckRequested()) {
//                            Map<String, Object> response = new HashMap<>();
//                            response.put("success", false);
//                            response.put("errorMessage", e.getMessage());
//                            response.put("data", null);
//                            response.put("movedToRoom", false);
//                            ackRequest.sendAckData(response);
//                        }
//                    }
//                }
//        );
//    }
//}