package com.ssafy.undaied.socket.room.handler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.room.dto.request.RoomCreateRequestDto;
import com.ssafy.undaied.socket.room.dto.request.RoomEnterRequestDto;
import com.ssafy.undaied.socket.room.dto.request.RoomLeaveRequestDto;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import com.ssafy.undaied.socket.room.dto.response.RoomEnterResponseDto;
import com.ssafy.undaied.socket.room.service.RoomService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ssafy.undaied.socket.common.constant.EventType.*;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.ROOM_KEY_PREFIX;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomHandler {

//    private final SocketIOServer server;
    private final SocketIONamespace namespace;  // 추가
    private final RoomService roomService;
    private final LobbyService lobbyService;

    @PostConstruct
    private void init() {
        namespace.addEventListener(CREATE_ROOM_AT_LOBBY.getValue(), RoomCreateRequestDto.class,
                (client, data, ackRequest) -> {
                    try {

                        // 방 생성.
                        RoomCreateResponseDto responseRoomData = roomService.createRoom(data, client);
                        log.info("Room created - ID: {}, Title: {}", responseRoomData.getRoomId(), responseRoomData.getRoomTitle());

                        // 로비에 데이터 보내주기
                        LobbyUpdateResponseDto lobbyUpdateResponseDto = lobbyService.sendEventRoomCreate(responseRoomData, client);
                        if(lobbyUpdateResponseDto != null) {
                            namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                        }

                        // 방을 생성한 클라이언트에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", responseRoomData.getRoomId());
                            ackRequest.sendAckData(response);
                        }

                    } catch (Exception e) {
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }
                    }
                }
        );

        namespace.addEventListener(LEAVE_ROOM_EMIT.getValue(), RoomLeaveRequestDto.class,
                (client, data, ackRequest) -> {
                    try {

                        LobbyUpdateResponseDto lobbyUpdateResponseDto = roomService.leaveRoom(data.getRoomId(), client);

                        // 로비에 데이터 보내주기
                        if(lobbyUpdateResponseDto != null) {
                            namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                        }

                        // 방을 생성한 클라이언트에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }

                    } catch (Exception e) {
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }
                    }
                }
        );

        namespace.addEventListener(ENTER_ROOM_EMIT.getValue(), RoomEnterRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        RoomEnterResponseDto roomEnterResponseDto = roomService.enterRoom(client, data.getRoomId(), data.getRoomPassword());

                        // 로비에 데이터 보내주기
                        LobbyUpdateResponseDto lobbyUpdateResponseDto = lobbyService.sendEventRoomEnter(roomEnterResponseDto, client);
                        if(lobbyUpdateResponseDto != null) {
                            namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                        }

                        // 방에 입장한 클라이언트에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("enterId", roomEnterResponseDto.getEnterId());
                            response.put("data", roomEnterResponseDto);
                            ackRequest.sendAckData(response);
                        }

                        // 방에 있는 모든 사용자에게 업데이트된 정보 전송
                        String key = ROOM_KEY_PREFIX + data.getRoomId();
                        namespace.getRoomOperations(key).sendEvent(ENTER_ROOM_SEND.getValue(), roomEnterResponseDto.getRoom());

                    } catch (Exception e) {
                        log.error("Room enter failed: {}", e.getMessage());
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }
                    }

                }
        );

    }
}
