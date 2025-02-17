package com.ssafy.undaied.socket.lobby.handler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyRoomListResponseDto;
import com.ssafy.undaied.socket.lobby.dto.response.UpdateData;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.room.service.RoomService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ssafy.undaied.socket.common.constant.EventType.ROOM_LIST_AT_LOBBY;

@Component
@Slf4j
@RequiredArgsConstructor
public class LobbyHandler {

    private final SocketIONamespace namespace;
    private final RoomService roomService;
    private final LobbyService lobbyService;

    @PostConstruct
    private void init() {
        namespace.addEventListener(ROOM_LIST_AT_LOBBY.getValue(), Object.class,
                (client, data, ackRequest) -> {
                    try {
                        log.debug("lobby:room:list emit 이벤트 발생 - userId: {}", (Integer) client.get("userId"));

                        // 방에 있었다면 방에서 전부 나가고 로비에 입장
                        roomService.clientLeaveAllRooms(client);
                        log.debug("성공적으로 모든 방에서 퇴장하였습니다. - userId: {}", (Integer) client.get("userId"));
                        lobbyService.joinLobby(client);

                        LobbyRoomListResponseDto responseData = roomService.findWaitingRoomList();
                        log.debug("성공적으로 대기중인 방 리스트를 찾았습니다.");

                        // 로비에 입장한 사용자에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", responseData);
                            ackRequest.sendAckData(response);
                            log.debug("lobby:room:list에 대한 ack응답 성공적으로 전송 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }

                    } catch (Exception e) {
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                            log.debug("lobby:room:list에 대한 ack응답 정송 실패 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }
                    }
                }
        );
    }

}
