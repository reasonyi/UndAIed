package com.ssafy.undaied.socket.room.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.room.dto.Room;
import com.ssafy.undaied.socket.room.dto.request.RoomCreateRequestDto;
import com.ssafy.undaied.socket.room.dto.request.RoomEnterRequestDto;
import com.ssafy.undaied.socket.room.dto.request.RoomLeaveRequestDto;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import com.ssafy.undaied.socket.room.service.RoomService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ssafy.undaied.socket.common.constant.EventType.*;
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
                        // 방 생성.
                        RoomCreateResponseDto responseRoomData = roomService.createRoom(data, client);
                        log.info("Room created - ID: {}, Title: {}", responseRoomData.getRoomId(), responseRoomData.getRoomTitle());
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

        server.addEventListener(LEAVE_ROOM.getValue(), RoomLeaveRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        roomService.leaveRoom(data.getRoomId(), data.getEnterId(), client);
                    } catch (Exception e) {
                        log.error("Failed to send room information: {}", e.getMessage());
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", SocketErrorCode.SOCKET_EVENT_ERROR.getStatus());
                        errorData.put("message", "방 정보 전송에 실패했습니다.");
                        client.sendEvent("error", errorData);
                    }
                }
        );

        server.addEventListener(ENTER_ROOM_AT_LOBBY.getValue(), RoomEnterRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        System.out.println("여기 들어올까");
                        roomService.enterRoom(client, data.getRoomId(), data.getRoomPassword());
                    } catch (Exception e) {
                        log.error("Room enter failed: {}", e.getMessage());
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", e instanceof SocketException ?
                                ((SocketException) e).getErrorCode().getStatus() :
                                SocketErrorCode.SOCKET_EVENT_ERROR.getStatus());
                        errorData.put("message", e instanceof SocketException ?
                                ((SocketException) e).getErrorCode().getMessage() :
                                "방 입장 실패");
                        client.sendEvent("error", errorData);
                    }

                }
        );

    }
}
