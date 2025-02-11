package com.ssafy.undaied.socket.room.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.handler.SocketIOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;
import com.ssafy.undaied.socket.lobby.dto.response.UpdateData;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.room.dto.Room;
import com.ssafy.undaied.socket.room.dto.RoomUser;
import com.ssafy.undaied.socket.room.dto.request.RoomCreateRequestDto;
import com.ssafy.undaied.socket.room.dto.response.RoomCreateResponseDto;
import com.ssafy.undaied.socket.room.dto.response.RoomEnterResponseDto;
import com.ssafy.undaied.socket.room.dto.response.RoomUserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

            // 현재 클라이언트의 방 상태 로깅
            Set<String> currentRooms = client.getAllRooms();
            log.debug("Current client rooms before creation: {}", currentRooms);

            // 로비 체크
            if (!lobbyService.isUserInLobby(client)) {
                log.error("User attempt to create room while not in lobby");
                throw new SocketException(USER_ALREADY_IN_ROOM);
            }

            Long roomId = jsonRedisTemplate.opsForValue().increment(ROOM_SEQUENCE_KEY);
            log.info("Generated room ID: {}", roomId);

            int hostId;
            int profileImage;
            try {
                hostId = Integer.parseInt(client.get("userId"));
                profileImage = Integer.parseInt(client.get("profileImage"));
            } catch (NumberFormatException e) {
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            if (!userRepository.existsById(hostId)) {
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            String nickname = client.get("nickname");
            if (nickname == null) {
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            List<RoomUser> users = new ArrayList<>();
            users.add(RoomUser.builder()
                    .enterId(0)
                    .userId(client.get("userId"))
                    .isHost(true)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .build());

            Room room = Room.builder()
                    .roomId(roomId)
                    .roomTitle(request.getRoomTitle())
                    .isPrivate(request.getIsPrivate())
                    .roomPassword(request.getRoomPassword())
                    .playing(false)
                    .currentPlayers(users)
                    .build();

            String key = ROOM_KEY_PREFIX + roomId;

            // rooms 네임스페이스에 방 정보 저장
            String roomKey = ROOM_LIST + key;  // "rooms:room:1"
            jsonRedisTemplate.opsForValue().set(roomKey, room);

            // waiting 리스트에 방 키만 추가
            String waitingKey = WAITING_LIST + key;  // "waiting:room:1"
            jsonRedisTemplate.opsForValue().set(waitingKey, roomId.toString());

            log.info("Room created - ID: {}, Title: {}", roomId, room.getRoomTitle());

            client.leaveRoom(LOBBY_ROOM);
            client.joinRoom(key);

            List<RoomUserResponseDto> userResponseDtos = room.getCurrentPlayers().stream()
                    .map(user -> RoomUserResponseDto.builder()
                            .enterId(user.getEnterId())
                            .isHost(user.getIsHost())
                            .nickname(user.getNickname())
                            .profileImage(user.getProfileImage())
                            .build())
                    .collect(Collectors.toList());

            return RoomCreateResponseDto.builder()
                    .roomId(room.getRoomId())
                    .roomTitle(room.getRoomTitle())
                    .isPrivate(room.getIsPrivate())
                    .playing(false)
                    .currentPlayers(userResponseDtos)  // RoomUserResponseDto 리스트로 변경
                    .build();

        } catch (SocketException e) {
            log.error("Failed to create room with errorCode: {}", e.getErrorCode());
            log.error("Exception message: {}", e.getMessage());
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Exception details:", e);
            log.error("Stack trace: ", (Object[]) e.getStackTrace()); // 디버깅용으로 남겨두기, 마지막에 코드 클리닝할 때 주석처리 or 삭제제
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating room: {}", e.getMessage());
            throw new SocketException(CREATE_ROOM_FAILED);
        }
    }

    public LobbyUpdateResponseDto leaveRoom(Long roomId, SocketIOClient client) throws SocketException {
        try {
            String key = ROOM_KEY_PREFIX + roomId;
            String roomKey = ROOM_LIST + key;  // "rooms:room:1"
            String waitingKey = WAITING_LIST + key;  // "waiting:room:1"

            // Redis에서 rooms: 네임스페이스의 방 정보 조회
            Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
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
            String userIdStr = client.get("userId");
            int userId = Integer.parseInt(userIdStr);

            boolean isHost = currentPlayers.stream()
                    .filter(user -> user.getUserId().equals(userId))  // userId로 비교
                    .findFirst()
                    .map(RoomUser::getIsHost)
                    .orElse(false);

            // 현재 플레이어 목록에서 해당 유저 제거
            currentPlayers.removeIf(user -> user.getUserId().equals(userId));

            UpdateData updateData = UpdateData
                    .builder()
                    .roomId(room.getRoomId())
                    .roomTitle(room.getRoomTitle())
                    .isPrivate(room.getIsPrivate())
                    .currentPlayerNum(currentPlayers.size())
                    .playing(false)
                    .build();

            LobbyUpdateResponseDto updateResponseDto = LobbyUpdateResponseDto
                    .builder()
                    .type("update")
                    .data(updateData)
                    .build();

            // 만약 나간 유저가 호스트였고, 남은 유저가 있다면 첫 번째 유저를 호스트로 지정
            if (isHost && !currentPlayers.isEmpty()) {
                currentPlayers.get(0).setIsHost(true);
            }

            // 방에서 내보내기
            client.leaveRoom(roomKey);  // rooms:room:1 형태의 키로 방 나가기

            // 방에 남은 유저가 없으면 rooms:와 waiting: 모두에서 방 삭제
            if (currentPlayers.isEmpty()) {
                jsonRedisTemplate.delete(roomKey);  // rooms:room:1 삭제
                // waiting:에 해당 방이 있는지 확인 후 삭제
                if (Boolean.TRUE.equals(jsonRedisTemplate.hasKey(waitingKey))) {
                    jsonRedisTemplate.delete(waitingKey);
                }
                log.info("Room deleted - ID: {}", roomId);
                updateResponseDto.setType("delete");
            } else {
                // 방 정보 업데이트 (rooms: 네임스페이스)
                room.setCurrentPlayers(currentPlayers);
                jsonRedisTemplate.opsForValue().set(roomKey, room);
                log.info("User left room - Room ID: {}, User ID: {}", roomId, client.get("userId"));
                // 방 안에 남아있는 유저들에게 알림
                leaveRoomAlarmToAnotherClient(roomKey);
            }

            // 로비로 돌아가기
            lobbyService.joinLobby(client);
            return updateResponseDto;
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

        // 기존의 leaveRoom 메서드 호출
        leaveRoom(roomId, client);
    }

    public void leaveRoomAlarmToAnotherClient(String key) throws SocketException {
        Object roomObj = jsonRedisTemplate.opsForValue().get(key);
        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room == null) {
            throw new SocketException(ROOM_NOT_FOUND);
        }

        // Room 유저 정보를 RoomUserResponseDto로 변환
        List<RoomUserResponseDto> userResponseDtos = room.getCurrentPlayers().stream()
                .map(user -> RoomUserResponseDto.builder()
                        .enterId(user.getEnterId())
                        .isHost(user.getIsHost())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .collect(Collectors.toList());

        // RoomCreateResponseDto 생성 (비밀번호 제외)
        RoomCreateResponseDto responseDto = RoomCreateResponseDto.builder()
                .roomId(room.getRoomId())
                .roomTitle(room.getRoomTitle())
                .isPrivate(room.getIsPrivate())
                .playing(room.getPlaying())
                .currentPlayers(userResponseDtos)
                .build();

        server.getRoomOperations(key).sendEvent(LEAVE_ROOM.getValue(), responseDto);
        log.info("Room information sent to room {} users", key);
    }

    // 유저가 특정 방에 있는지 확인하는 메서드 추가
    public boolean isUserInRoom(SocketIOClient client, Long roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        Set<String> rooms = new HashSet<>(client.getAllRooms());

        return rooms.contains(key);
    }


    public RoomEnterResponseDto enterRoom(SocketIOClient client, Long roomId, Integer password) throws SocketException {
        String key = ROOM_KEY_PREFIX + roomId;
        String roomKey = ROOM_LIST + key;  // "rooms:room:1"

        // Redis에서 rooms: 네임스페이스의 방 정보 조회
        Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room == null) {
            throw new SocketException(ROOM_NOT_FOUND);
        }

        Integer enterId = 0;

        // 유저가 방에 있는지 확인
        if (!isUserInRoom(client, roomId)) {
            // 방이 private인 경우 비밀번호 체크
            if (room.getIsPrivate() && !room.getRoomPassword().equals(password)) {
                throw new SocketException(INVALID_ROOM_PASSWORD);
            }

            // 새로운 RoomUser 생성을 위한 유저 정보 가져오기
            String nickname = client.get("nickname");
            int userId;
            int profileImage;
            try {
                userId = Integer.parseInt(client.get("userId"));
                profileImage = Integer.parseInt(client.get("profileImage"));
            } catch (NumberFormatException e) {
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            if (nickname == null) {
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            // 현재 방의 최대 enterId를 찾아서 +1
            int newEnterId = room.getCurrentPlayers().stream()
                    .mapToInt(RoomUser::getEnterId)
                    .max()
                    .orElse(-1) + 1;  // 방이 비어있다면 0이 됨

            enterId = newEnterId;

            RoomUser newUser = RoomUser.builder()
                    .enterId(newEnterId)
                    .userId(userId)
                    .isHost(false)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .build();

            room.getCurrentPlayers().add(newUser);
            jsonRedisTemplate.opsForValue().set(roomKey, room);

            // 입장하는 사용자를 이동시키기
            client.leaveRoom(LOBBY_ROOM);
            client.joinRoom(roomKey);  // rooms:room:1 형태의 키로 방 입장
        }

        // RoomUser를 RoomUserResponseDto로 변환
        List<RoomUserResponseDto> userResponseDtos = room.getCurrentPlayers().stream()
                .map(user -> RoomUserResponseDto.builder()
                        .enterId(user.getEnterId())
                        .isHost(user.getIsHost())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .collect(Collectors.toList());

        // 비밀번호를 제외한 방 정보로 ResponseDto 생성
        RoomCreateResponseDto roomResponse = RoomCreateResponseDto.builder()
                .roomId(room.getRoomId())
                .roomTitle(room.getRoomTitle())
                .isPrivate(room.getIsPrivate())
                .playing(room.getPlaying())
                .currentPlayers(userResponseDtos)
                .build();

        return RoomEnterResponseDto.builder()
                .enterId(enterId)
                .room(roomResponse)  // RoomCreateResponseDto로 변경
                .build();
    }

}
