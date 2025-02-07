package com.ssafy.undaied.socket.room.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.exception.ErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
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
import java.util.List;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final LobbyService lobbyService;
    private final UserRepository userRepository;


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

            lobbyService.leaveLobby(client);

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
                    .currentPlayers(users)
                    .build();

            String key = ROOM_KEY_PREFIX + roomId;
            jsonRedisTemplate.opsForValue().set(key, room);
            log.info("Room created - ID: {}, Title: {}", roomId, room.getRoomTitle());

            return RoomCreateResponseDto.builder()
                    .roomId(room.getRoomId())
                    .roomTitle(room.getRoomTitle())
                    .isPrivate(room.getIsPrivate())
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

//    public void leaveRoom(Long roomId, int enterId, SocketIOClient client) throws SocketException {
//        try {
//            String key = ROOM_KEY_PREFIX + roomId;
//
//            // Redis에서 방 정보 조회
//            Room room = (Room) jsonRedisTemplate.opsForValue().get(key);
//            if (room == null) {
//                throw new SocketException(ROOM_NOT_FOUND);
//            }
//
//            List<RoomUser> currentPlayers = room.getCurrentPlayers();
//
//            // 나가려는 유저가 방에 있는지 확인
//            boolean userExists = currentPlayers.stream()
//                    .anyMatch(user -> user.getEnterId() == enterId);
//            if (!userExists) {
//                throw new SocketException(USER_NOT_IN_ROOM);
//            }
//
//            // 나가려는 유저가 호스트인지 확인
//            boolean isHost = currentPlayers.stream()
//                    .filter(user -> user.getEnterId() == enterId)
//                    .findFirst()
//                    .map(RoomUser::getIsHost)
//                    .orElse(false);
//
//            // 현재 플레이어 목록에서 해당 유저 제거
//            currentPlayers.removeIf(user -> user.getEnterId() == enterId);
//
//            // 만약 나간 유저가 호스트였고, 남은 유저가 있다면 첫 번째 유저를 호스트로 지정
//            if (isHost && !currentPlayers.isEmpty()) {
//                currentPlayers.get(0).setIsHost(true);
//            }
//
//            // 방에 남은 유저가 없으면 방 삭제
//            if (currentPlayers.isEmpty()) {
//                jsonRedisTemplate.delete(key);
//                log.info("Room deleted - ID: {}", roomId);
//            } else {
//                // 방 정보 업데이트
//                room.setCurrentPlayers(currentPlayers);
//                jsonRedisTemplate.opsForValue().set(key, room);
//                log.info("User left room - Room ID: {}, User ID: {}", roomId, enterId);
//            }
//
//            // 로비로 돌아가기
//            lobbyService.joinLobby(client);
//
//        } catch (SocketException e) {
//            log.error("Failed to leave room: {}", e.getErrorCode().getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error while leaving room: {}", e.getMessage());
//            throw new SocketException(LEAVE_ROOM_FAILED);
//        }
//    }


    public void leaveGameRoom(SocketIOClient client, String room) {
        client.leaveRoom(room);
        log.info("User {} (sessionId: {}) left gameRoom", client.get("userId"), client.getSessionId());
    }

//    public RoomUser enterRoom(Long roomId, Integer userId, String nickname) {
//        String userOrderKey = "room:" + roomId + ":userOrder";
//        int enterId = redisTemplate.opsForValue().increment("room:" + roomId + ":userCount").intValue();
//
//        RoomUser roomUser = new RoomUser();
//        roomUser.setEnterId(enterId);
//        roomUser.setNickname(nickname);
//        roomUser.setHost(enterId == 1); // 첫 번째 입장한 유저가 호스트
//
//        return roomUser;
//    }


}
