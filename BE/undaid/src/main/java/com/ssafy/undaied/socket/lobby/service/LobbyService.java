package com.ssafy.undaied.socket.lobby.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;
import com.ssafy.undaied.socket.lobby.dto.response.UpdateData;
import com.ssafy.undaied.socket.room.dto.Room;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import com.ssafy.undaied.socket.room.dto.response.RoomEnterResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

import java.util.HashSet;
import java.util.Set;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;

@Service
@Slf4j
@RequiredArgsConstructor
public class LobbyService {
    private final RouterFunctionMapping routerFunctionMapping;

    /**
     * 클라이언트를 로비에 입장시킵니다.
     */
    public void joinLobby(SocketIOClient client) {
        // 로비에 입장
        client.joinRoom(LOBBY_ROOM);
        log.info("클라이언트 로비 입장 - userId:{}, 현재 방 목록: {}", client.get("userId"), client.getAllRooms());
    }

    /**
     * 클라이언트를 로비에서 퇴장시킵니다.
     */
//    public void leaveLobby(SocketIOClient client) {
//        client.leaveRoom(LOBBY_ROOM);
//        log.info("User {} (sessionId: {}) left lobby", client.get("userId"), client.getSessionId());
//    }

    public boolean isUserInLobby(SocketIOClient client) {
        Set<String> rooms = new HashSet<>(client.getAllRooms());
        log.debug("유저 {} 가 로비에 있는지 확인 중 - 모든 방 목록: {}", client.get("userId"), rooms);

        rooms.remove("");   // 빈 방 제거

        boolean inLobby = rooms.size() == 1 && rooms.contains(LOBBY_ROOM);

        if(inLobby) {
            log.debug("유저가 로비에 있음 - userId: {}", (Integer) client.get("userId"));
        } else {
            log.debug("유저가 로비에 없음 - userId: {}", (Integer) client.get("userId"));
        }
        return inLobby;
    }

    public LobbyUpdateResponseDto sendEventRoomCreate(RoomCreateResponseDto responseDto, SocketIOClient client) {

        // 비밀방인 경우 로비에 보내지 않는다.
        if(responseDto.getIsPrivate()) {
            log.debug("비밀방이기 때문에 로비에 데이터를 전송하지 않습니다. - roomId: {}", responseDto.getRoomId());
            return null;
        }

        log.debug("로비에 방 업데이트 정보를 보내기 위한 데이터 준비 - 방 아이디: {} 방 제목: {} 비밀방 여부: {} 인원 수: {} 플레이 중: {}",
                responseDto.getRoomId(),
                responseDto.getRoomTitle(),
                false,
                responseDto.getCurrentPlayers().size(),
                responseDto.getPlaying());

        UpdateData updateData = UpdateData.builder()
                .roomId(responseDto.getRoomId())
                .roomTitle(responseDto.getRoomTitle())
                .isPrivate(responseDto.getIsPrivate())
                .currentPlayerNum(responseDto.getCurrentPlayers().size())
                .playing(responseDto.getPlaying())
                .build();

        return LobbyUpdateResponseDto.builder()
                .type("create")
                .data(updateData)
                .build();
    }

    public LobbyUpdateResponseDto sendEventRoomEnter(RoomEnterResponseDto responseDto, SocketIOClient client) {

        RoomCreateResponseDto room = responseDto.getRoom();

        UpdateData updateData = UpdateData.builder()
                .roomId(room.getRoomId())
                .roomTitle(room.getRoomTitle())
                .isPrivate(room.getIsPrivate())
                .currentPlayerNum(room.getCurrentPlayers().size())
                .playing(room.getPlaying())
                .build();

        return LobbyUpdateResponseDto.builder()
                .type("update")
                .data(updateData)
                .build();
    }

}