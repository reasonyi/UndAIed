package com.ssafy.undaied.socket.room.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.exception.ErrorCode;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RedisTemplate<String, Room> redisTemplate;
    private final LobbyService lobbyService;
    private final UserRepository userRepository;


    public RoomCreateResponseDto createRoom(RoomCreateRequestDto request, SocketIOClient client) {
        try {
            if (request == null) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
            }

            if (!Boolean.TRUE.equals(redisTemplate.hasKey(ROOM_SEQUENCE_KEY))) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
            }

            lobbyService.leaveLobby(client);

            Long roomId = redisTemplate.opsForValue().increment(ROOM_SEQUENCE_KEY);
            log.info("Generated room ID: {}", roomId);

            Object userIdObj = client.get("userId");
            if (userIdObj == null) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
            }

            // 유저가 로비에 있는지 확인
            if (!lobbyService.isUserInLobby(client)) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
            }

            int hostId;
            try {
                hostId = Integer.parseInt(userIdObj.toString());
            } catch (NumberFormatException e) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
            }

            if (!userRepository.existsById(hostId)) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
            }

            String nickname = client.get("nickname");
            if (nickname == null) {
                throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
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
            redisTemplate.opsForValue().set(key, room);
            log.info("Room created - ID: {}, Title: {}", roomId, room.getRoomTitle());

            return RoomCreateResponseDto.builder()
                    .roomId(room.getRoomId())
                    .roomTitle(room.getRoomTitle())
                    .isPrivate(room.getIsPrivate())
                    .currentPlayers(room.getCurrentPlayers())
                    .build();

        } catch (BaseException e) {
            log.error("Failed to create room: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating room: {}", e.getMessage());
            throw new BaseException(ErrorCode.CREATE_ROOM_FAILED);
        }
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
