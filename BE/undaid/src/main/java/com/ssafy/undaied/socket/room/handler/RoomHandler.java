package com.ssafy.undaied.socket.room.handler;

import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.room.dto.Room;
import com.ssafy.undaied.socket.room.dto.request.RoomCreateRequestDto;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import com.ssafy.undaied.socket.room.service.RoomService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ssafy.undaied.socket.common.constant.EventType.CREATE_ROOM;
import static com.ssafy.undaied.socket.common.constant.EventType.CREATE_ROOM_AT_LOBBY;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.ROOM_KEY_PREFIX;

import com.ssafy.undaied.socket.common.exception.SocketErrorCode;

import java.util.HashMap;
import java.util.Map;

import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.CREATE_ROOM_FAILED;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomHandler {

    private final SocketIOServer server;
    private final RoomService roomService;

    @PostConstruct
    private void init() {
        server.addEventListener(CREATE_ROOM_AT_LOBBY.getValue(), RoomCreateRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        RoomCreateResponseDto responseRoomData = roomService.createRoom(data, client);
                        log.info("Room created - ID: {}, Title: {}",
                                responseRoomData.getRoomId(),
                                responseRoomData.getRoomTitle());

                        client.joinRoom(ROOM_KEY_PREFIX + responseRoomData.getRoomId());

                        client.sendEvent(CREATE_ROOM.getValue(), responseRoomData); // 방을 생성한 클라이언트에게 데이터 전송

                        ////                        // 로비에 있는 사람들에게 방 생성 알림
                        ////                        server.getRoomOperations("lobby")
                        ////                                .sendEvent(SocketEvent.ROOM_CREATED.getValue(), responseRoomData);
                    } catch (Exception e) {
                        log.error("Room creation failed: {}", e.getMessage());
                        // connection을 끊지 않고 error 이벤트 emit
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", CREATE_ROOM_FAILED.getStatus());
                        errorData.put("message", CREATE_ROOM_FAILED.getMessage());
                        client.sendEvent("error", errorData);
                        // throw하지 않음으로써 connection 유지
                    }
                }
        );
    }
}
