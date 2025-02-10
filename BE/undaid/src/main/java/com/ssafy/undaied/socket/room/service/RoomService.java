package com.ssafy.undaied.socket.room.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.room.dto.Room;
import com.ssafy.undaied.socket.room.dto.RoomUser;
import com.ssafy.undaied.socket.room.dto.request.RoomCreateRequestDto;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ssafy.undaied.socket.common.constant.EventType.*;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final LobbyService lobbyService;
    private final UserRepository userRepository;
    private final SocketIOServer server;
    private final ObjectMapper objectMapper;


    public RoomCreateResponseDto createRoom(RoomCreateRequestDto request, SocketIOClient client) throws SocketException {
        try {
            if (request == null) {
                throw new SocketException(CREATE_ROOM_FAILED);
            }

            if (!Boolean.TRUE.equals(jsonRedisTemplate.hasKey(ROOM_SEQUENCE_KEY))) {
                throw new SocketException(CREATE_ROOM_FAILED);
            }

            // 유저가 로비에 있는지 확인
            if (!lobbyService.isUserInLobby(client)) {
                throw new SocketException(USER_ALREADY_IN_ROOM);
            }

            Long roomId = jsonRedisTemplate.opsForValue().increment(ROOM_SEQUENCE_KEY);
            log.info("Generated room ID: {}", roomId);

            Object userIdObj = client.get("userId");
            if (userIdObj == null) {
                throw new SocketException(CREATE_ROOM_FAILED);
            }

            int hostId;
            try {
                hostId = Integer.parseInt(userIdObj.toString());
            } catch (NumberFormatException e) {
                throw new SocketException(CREATE_ROOM_FAILED);
            }

            if (!userRepository.existsById(hostId)) {
                throw new SocketException(CREATE_ROOM_FAILED);
            }

            String nickname = client.get("nickname");
            if (nickname == null) {
                throw new SocketException(CREATE_ROOM_FAILED);
            }

            List<RoomUser> users = new ArrayList<>();
            users.add(RoomUser.builder()
                    .enterId(0)
                    .isHost(true)
                    .nickname(nickname)
                    .build());

            Room room = Room.builder()
                    .roomId(roomId)
                    .roomTitle(request.getRoomTitle())
                    .isPrivate(request.getIsPrivate())
                    .roomPassword(request.getRoomPassword())
                    .playing(false)
                    .currentPlayers(users)
                    .build();

            // 방을 저장할 때 레디스에서 분리를...
            String key = ROOM_KEY_PREFIX + roomId;
            jsonRedisTemplate.opsForValue().set(key, room);
            log.info("Room created - ID: {}, Title: {}", roomId, room.getRoomTitle());

            client.leaveRoom(LOBBY_ROOM);
            client.joinRoom(key);

            return RoomCreateResponseDto.builder()
                    .roomId(room.getRoomId())
                    .roomTitle(room.getRoomTitle())
                    .isPrivate(room.getIsPrivate())
                    .playing(false)
                    .currentPlayers(room.getCurrentPlayers())
                    .build();

        } catch (SocketException e) {
            log.error("Failed to create room with errorCode: {}", e.getErrorCode());
            log.error("Exception message: {}", e.getMessage());
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Exception details:", e);
            log.error("Stack trace: ", e.getStackTrace());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating room: {}", e.getMessage());
            throw new SocketException(CREATE_ROOM_FAILED);
        }
    }

    public void leaveRoom(Long roomId, int enterId, SocketIOClient client) throws SocketException {
        try {
            String key = ROOM_KEY_PREFIX + roomId;

            // Redis에서 방 정보 조회
            Object roomObj = jsonRedisTemplate.opsForValue().get(key);
            Room room = objectMapper.convertValue(roomObj, Room.class);
            if (room == null) {
                throw new SocketException(ROOM_NOT_FOUND);
            }

            List<RoomUser> currentPlayers = room.getCurrentPlayers();

            // 나가려는 유저가 방에 있는지 확인
            if (!isUserInRoom(client, roomId)) {
                throw new SocketException(USER_NOT_IN_ROOM);
            }
            // 나가려는 유저가 호스트인지 확인
            boolean isHost = currentPlayers.stream()
                    .filter(user -> user.getEnterId() == enterId)
                    .findFirst()
                    .map(RoomUser::getIsHost)
                    .orElse(false);

            // 현재 플레이어 목록에서 해당 유저 제거
            currentPlayers.removeIf(user -> user.getEnterId() == enterId);

            // 만약 나간 유저가 호스트였고, 남은 유저가 있다면 첫 번째 유저를 호스트로 지정
            if (isHost && !currentPlayers.isEmpty()) {
                currentPlayers.get(0).setIsHost(true);
            }

            // 방에서 내보내기.
            client.leaveRoom(key);

            // 방에 남은 유저가 없으면 방 삭제
            if (currentPlayers.isEmpty()) {
                jsonRedisTemplate.delete(key);
                log.info("Room deleted - ID: {}", roomId);
            } else {
                // 방 정보 업데이트
                room.setCurrentPlayers(currentPlayers);
                jsonRedisTemplate.opsForValue().set(key, room);
                log.info("User left room - Room ID: {}, User ID: {}", roomId, enterId);
                // 방 안에 남아있는 유저들에게 알림.
                leaveRoomAlarmToAnotherClient(key);
            }

            // 로비로 돌아가기
            lobbyService.joinLobby(client);

        } catch (SocketException e) {
            log.error("Failed to leave room: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while leaving room: {}", e.getMessage());
            throw new SocketException(LEAVE_ROOM_FAILED);
        }
    }

    public void leaveRoom(SocketIOClient client, String room) throws SocketException {
        // "room:" 접두사 이후의 문자열을 추출하여 Long으로 변환
        Long roomId = Long.parseLong(room.substring(ROOM_KEY_PREFIX.length()));
        int enterId = client.get("userId");  // client에서 userId를 가져옴

        // 기존의 leaveRoom 메서드 호출
        leaveRoom(roomId, enterId, client);
    }

    public void leaveRoomAlarmToAnotherClient(String key) throws SocketException {
        Object roomObj = jsonRedisTemplate.opsForValue().get(key);
        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room == null) {
            throw new SocketException(ROOM_NOT_FOUND);
        }
        server.getRoomOperations(key).sendEvent(LEAVE_ROOM.getValue(), room);
        log.info("Room information sent to room {} users", key);
    }

    // 유저가 특정 방에 있는지 확인하는 메서드 추가
    public boolean isUserInRoom(SocketIOClient client, Long roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        Set<String> rooms = new HashSet<>(client.getAllRooms());

        return rooms.contains(key);
    }


    public Room enterRoom(SocketIOClient client, Long roomId, Integer password) throws SocketException {
        String key = ROOM_KEY_PREFIX + roomId;

        // 레디스에서 방 정보 조회
        Object roomObj = jsonRedisTemplate.opsForValue().get(key);
        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room == null) {
            throw new SocketException(ROOM_NOT_FOUND);
        }

        // 유저가 방에 있는지 확인
        if (!isUserInRoom(client, roomId)) {

            // 방이 private인 경우 비밀번호 체크
            if (room.getIsPrivate() && !room.getRoomPassword().equals(password)) {
                throw new SocketException(INVALID_ROOM_PASSWORD);
            }

            // 3. 새로운 RoomUser 생성
            String nickname = client.get("nickname");
            if (nickname == null) {
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            // 현재 방의 최대 enterId를 찾아서 +1
            int newEnterId = room.getCurrentPlayers().stream()
                    .mapToInt(RoomUser::getEnterId)
                    .max()
                    .orElse(-1) + 1;  // 방이 비어있다면 0이 됨

            RoomUser newUser = RoomUser.builder()
                    .enterId(newEnterId)
                    .isHost(false)
                    .nickname(nickname)
                    .build();

            room.getCurrentPlayers().add(newUser);
            jsonRedisTemplate.opsForValue().set(key, room);

            // 입장하는 사용자를 이동시키기.
            client.leaveRoom(LOBBY_ROOM);
            client.joinRoom(key);
        }

        // 방에 있는 모든 사용자에게 업데이트된 정보 전송
        server.getRoomOperations(key).sendEvent(ENTER_ROOM.getValue(), room);

        return room;
    }


}
