package com.ssafy.undaied.socket.room.handler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.common.exception.SocketException;
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
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.CREATE_ROOM_FAILED;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.LEAVE_ROOM_FAILED;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomHandler {

    private final SocketIONamespace namespace;  // 추가
    private final RoomService roomService;
    private final LobbyService lobbyService;

    @PostConstruct
    private void init() {
        namespace.addEventListener(CREATE_ROOM_AT_LOBBY.getValue(), RoomCreateRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        if (data == null) {
                            log.error("방 생성을 위한 데이터가 비어있습니다.");
                            throw new SocketException(CREATE_ROOM_FAILED);
                        }

                        log.info("lobby:room:create 이벤트 도착. - 방을 생성한 userId: {}, roomTitle: {}", client.get("userId"), data.getRoomTitle());

                        // 방 생성.
                        RoomCreateResponseDto responseRoomData = roomService.createRoom(data, client);
                        log.info("성공적으로 방이 생성되었습니다. - roomId: {}, roomTitle: {}", responseRoomData.getRoomId(), responseRoomData.getRoomTitle());
                        log.debug("클라이언트가 현재 입장되어있는 방: {}", client.getAllRooms());

                        // 로비에 데이터 보내주기
                        LobbyUpdateResponseDto lobbyUpdateResponseDto = lobbyService.sendEventRoomCreate(responseRoomData, client);
                        if(lobbyUpdateResponseDto != null) {
                            namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                            log.debug("lobby:room:update sendEvent 실행. - eventType: {}, roomId: {}", lobbyUpdateResponseDto.getType(), lobbyUpdateResponseDto.getData().getRoomId());
                        }

                        // 방을 생성한 클라이언트에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", responseRoomData.getRoomId());
                            ackRequest.sendAckData(response);
                            log.debug("lobby:room:create에 대한 ack응답 성공적으로 전송 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }

                    } catch (Exception e) {
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                            log.error("lobby:room:create에 대한 ack응답 전송 실패 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }
                    }
                }
        );

        namespace.addEventListener(LEAVE_ROOM_EMIT.getValue(), RoomLeaveRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        if (data == null) {
                            log.error("방 퇴장을 위한 데이터가 비어있습니다.");
                            throw new SocketException(LEAVE_ROOM_FAILED);
                        }

                        log.info("room:leave:emit 이벤트 도착. - userId: {}, roomId: {}", client.get("userId"), data.getRoomId());


                        LobbyUpdateResponseDto lobbyUpdateResponseDto = roomService.leaveRoom(data.getRoomId(), client);
                        log.info("클라이언트가 성공적으로 방을 나갔습니다. - userId: {}, room: {}", client.get("userId"), data.getRoomId());

                        // 방에 있는 모든 사용자에게 업데이트된 정보 전송
                        String key = ROOM_KEY_PREFIX + data.getRoomId();
                        roomService.leaveRoomSystemChat(client.get("nickname"), key);   // 시스템 메시지 알림

                        // 로비에 데이터 보내주기
                        if(lobbyUpdateResponseDto != null) {
                            namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                            log.debug("lobby:room:update sendEvent 실행. - eventType: {}, roomId: {}", lobbyUpdateResponseDto.getType(), lobbyUpdateResponseDto.getData().getRoomId());
                        }

                        // 방을 생성한 클라이언트에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                            log.debug("room:leave:emit에 대한 ack응답 성공적으로 전송 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }

                    } catch (Exception e) {
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                            log.error("room:leave:emit에 대한 ack응답 전송 실패 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }
                    }
                }
        );

        namespace.addEventListener(ENTER_ROOM_EMIT.getValue(), RoomEnterRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        if (data == null) {
                            log.error("방 입장을 위한 데이터가 비어있습니다.");
                            throw new SocketException(CREATE_ROOM_FAILED);
                        }

                        log.info("room:enter:emit 이벤트 도착. - userId: {}, roomId: {}", client.get("userId"), data.getRoomId());
                        
                        RoomEnterResponseDto roomEnterResponseDto = roomService.enterRoom(client, data.getRoomId(), data.getRoomPassword());
                        log.debug("User enter room Successfully - userId: {}, roomId: {}", client.get("userId"), data.getRoomId());

                        // 로비에 데이터 보내주기
                        LobbyUpdateResponseDto lobbyUpdateResponseDto = lobbyService.sendEventRoomEnter(roomEnterResponseDto, client);
                        if(lobbyUpdateResponseDto != null) {
                            namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                            log.debug("lobby:room:update sendEvent 실행. - eventType: {}, roomId: {}", lobbyUpdateResponseDto.getType(), lobbyUpdateResponseDto.getData().getRoomId());
                        }

                        // 방에 입장한 클라이언트에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("enterId", roomEnterResponseDto.getEnterId());
                            response.put("data", roomEnterResponseDto);
                            ackRequest.sendAckData(response);
                            log.debug("room:enter:emit 에 대한 ack응답 성공적으로 전송 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }

                        // 방에 있는 모든 사용자에게 업데이트된 정보 전송
                        String key = ROOM_KEY_PREFIX + data.getRoomId();
                        namespace.getRoomOperations(key).sendEvent(ENTER_ROOM_SEND.getValue(), roomEnterResponseDto.getRoom());
                        roomService.enterRoomSystemChat(client.get("nickname"), key);   // 시스템 메시지 알림
                        log.info("room:enter:send 이벤트를 발생시켜 {}번 방 정보를 해당 방 안에 남은 유저들에게 알림.", key);

                    } catch (Exception e) {
                        log.error("Room enter failed: {}", e.getMessage());
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                            log.debug("room:enter:emit 에 대한 ack응답 전송 실패 - userId: {}, userNickname: {}", client.get("userId"), client.get("nickname"));
                        }
                    }

                }
        );

    }
}
