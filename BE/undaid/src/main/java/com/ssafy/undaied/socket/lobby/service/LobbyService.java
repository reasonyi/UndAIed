package com.ssafy.undaied.socket.lobby.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;
import com.ssafy.undaied.socket.lobby.dto.response.UpdateData;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;

@Service
@Slf4j
@RequiredArgsConstructor
public class LobbyService {
    /**
     * 클라이언트를 로비에 입장시킵니다.
     */
    public void joinLobby(SocketIOClient client) {
        client.joinRoom(LOBBY_ROOM);
        log.info("User {} (sessionId: {}) joined lobby", client.get("userId"), client.getSessionId());
    }

    /**
     * 클라이언트를 로비에서 퇴장시킵니다.
     */
    public void leaveLobby(SocketIOClient client) {
        client.leaveRoom(LOBBY_ROOM);
        log.info("User {} (sessionId: {}) left lobby", client.get("userId"), client.getSessionId());
    }

    public boolean isUserInLobby(SocketIOClient client) {
        Set<String> rooms = new HashSet<>(client.getAllRooms()); // 새로운 Set으로 복사
        rooms.remove(""); // 빈 room 제거

        if (rooms.size() != 1 || !rooms.contains(LOBBY_ROOM)) {
            return false;
        }
        return true;
    }

    public LobbyUpdateResponseDto sendEventRoomCreate(RoomCreateResponseDto responseDto, SocketIOClient client) {

        // 비밀방인 경우 로비에 보내지 않는다.
        if(responseDto.getIsPrivate()) return null;

        System.out.println("방 아이디: "+responseDto.getRoomId()+" 방 제목: "+responseDto.getRoomTitle()+" 비밀방 여부: "+ false +" 인원 수: "+ responseDto.getCurrentPlayers().size()+"플레이 중: "+responseDto.getPlaying());

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

}