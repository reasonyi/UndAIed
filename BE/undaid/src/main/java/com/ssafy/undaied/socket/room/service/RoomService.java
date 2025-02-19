package com.ssafy.undaied.socket.room.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.chat.dto.response.RoomChatResponseDto;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyRoomListResponseDto;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.ssafy.undaied.socket.common.constant.EventType.*;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final LobbyService lobbyService;
    private final UserRepository userRepository;
    private final SocketIONamespace namespace;  // 추가
//    private final SocketIOServer server;
    private final ObjectMapper objectMapper;

    private static final Integer PAGE_SIZE = 10;

    public RoomCreateResponseDto createRoom(RoomCreateRequestDto request, SocketIOClient client) throws SocketException {
        try {
            log.debug("방 생성을 시도하는중... - roomTitle: {}", request.getRoomTitle());
            // request는 이미 상위 메서드에서 null을 확인하기 때문에 null이 아닌 것만 이 메서드에 도착.

            if (!Boolean.TRUE.equals(jsonRedisTemplate.hasKey(ROOM_SEQUENCE_KEY))) {
                // 시퀀스 키가 없으면 새로 생성하고 0으로 초기화
                // 레디스 테스트 필요
                log.info("redis에 방 시퀀스 키가 없어 새로 추가합니다.");
                jsonRedisTemplate.opsForValue().set(ROOM_SEQUENCE_KEY, 0);
            }

//            // 현재 클라이언트의 방 상태 로깅
//            log.debug("방을 만들기 전 현재 유저 room 상태: {}", client.getAllRooms());

            // 로비 체크
            if (!lobbyService.isUserInLobby(client)) {
                log.error("유저가 로비에 있지 않은 상태에서 방 생성을 시도했습니다.");
                throw new SocketException(USER_ALREADY_IN_ROOM);
            }

            Long roomId = jsonRedisTemplate.opsForValue().increment(ROOM_SEQUENCE_KEY);
            log.debug("redis에서 방 생성에 성공했습니다. 생성한 방 id: {}", roomId);

            int hostId = (Integer) client.get("userId");
            int profileImage = (Integer) client.get("profileImage");

            if (!userRepository.existsById(hostId)) {
                log.debug("유저를 DB에서 찾을 수 없습니다. - userId: {}", hostId);
                throw new SocketException(USER_INFO_NOT_FOUND);
            }

            String nickname = client.get("nickname");

            List<RoomUser> users = new ArrayList<>();
            users.add(RoomUser.builder()
                    .enterId(0)
                    .userId(hostId)
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

//            log.debug("Built Room object: {}", room);

            String key = ROOM_KEY_PREFIX + roomId;  // room:1
//            log.debug("Generated key: {}", key);

            // rooms 네임스페이스에 방 정보 저장
            String roomKey = ROOM_LIST + key;  // "rooms:room:1"
            jsonRedisTemplate.opsForValue().set(roomKey, room);
            log.debug("레디스에 방 정보를 저장합니다. - roomKey: {}", roomKey);

            // waiting 리스트에 방 키만 추가
            String waitingKey = WAITING_LIST + key;  // "waiting:room:1"
            jsonRedisTemplate.opsForValue().set(waitingKey, roomId.toString());
            log.debug("레디스 대기방 목록에 방 정보를 저장합니다. - waitingKey: {}", waitingKey);

            client.leaveRoom(LOBBY_ROOM);
            log.debug("클라이언트를 로비에서 퇴장시켰습니다. - userId: {}", (Integer) client.get("userId"));
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
            log.error("방 생성에 실패했습니다.: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("방을 생성하는 동안 예상하지 못한 오류가 발생했습니다.: {}", e.getMessage());
            throw new SocketException(CREATE_ROOM_FAILED);
        }
    }

    public void clientLeaveAllRooms(SocketIOClient client) throws SocketException {
        log.debug("클라이언트가 모든 방에서 나가기를 시도합니다. - Client ID: {}", client.getSessionId());
        log.debug("모든 방에서 나가기 전 현재 입장되어있는 방: {}", client.getAllRooms());

        // 기존 방에서 모두 나가기
        Set<String> rooms = new HashSet<>(client.getAllRooms());
        rooms.remove("");
        for (String room : rooms) {
            if(room.startsWith(ROOM_KEY_PREFIX)) {
                Long roomId = Long.parseLong(room.substring(ROOM_KEY_PREFIX.length()));

                LobbyUpdateResponseDto lobbyUpdateResponseDto = leaveRoom(roomId, client);
                if(lobbyUpdateResponseDto != null) {
                    namespace.getRoomOperations(LOBBY_ROOM).sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), lobbyUpdateResponseDto);
                    log.debug("lobby:room:update sendEvent 실행. - eventType: {}, roomId: {}", lobbyUpdateResponseDto.getType(), lobbyUpdateResponseDto.getData().getRoomId());
                }
                client.leaveRoom(room);
            } else {
                client.leaveRoom(room);
            }
            log.info("클라이언트가 방을 나갔습니다. - userId: {}, room: {}", client.get("userId"), room);
        }
    }

    public LobbyUpdateResponseDto leaveRoom(Long roomId, SocketIOClient client) throws SocketException {
        try {
            log.debug("클라이언트가 대기방을 나가기를 시도합니다. - userId: {}, roomId: {}", client.get("userId"), roomId);

            String key = ROOM_KEY_PREFIX + roomId; // room:1
            String roomKey = ROOM_LIST + key;  // "rooms:room:1"
            String waitingKey = WAITING_LIST + key;  // "waiting:room:1"

            // Redis에서 rooms: 네임스페이스의 방 정보 조회
            Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
            Room room = objectMapper.convertValue(roomObj, Room.class);
            if (room == null) {
                log.error("찾는 방이 없습니다. - roomId: {}", roomId);
                throw new SocketException(ROOM_NOT_FOUND);
            }

            List<RoomUser> currentPlayers = room.getCurrentPlayers();
            // 나가려는 유저가 방에 있는지 확인
            if (!isUserInRoom(client, roomId)) {
                log.error("나가려는 유저가 나가려는 방에 없습니다. - userId: {}, roomId: {}", client.get("userId"), roomId);
                throw new SocketException(USER_NOT_IN_ROOM);
            }
            log.debug("나가려는 유저가 나가려는 방에 있는것을 확인했습니다! - userId: {}, roomId: {}", client.get("userId"), roomId);

            // 나가려는 유저가 호스트인지 확인
            log.debug("나가려는 유저가 방장인지 확인 중... - userId: {}, roomId: {}", client.get("userId"), roomId);
            int userId = (Integer) client.get("userId");

            boolean isHost = currentPlayers.stream()
                    .filter(user -> user.getUserId().equals(userId))  // userId로 비교
                    .findFirst()
                    .map(RoomUser::getIsHost)
                    .orElse(false);

            if(isHost) {
                log.debug("나가려는 유저는 방장입니다!- userId: {}, roomId: {}", client.get("userId"), roomId);
            } else {
                log.debug("나가려는 유저는 방장이 아닙니다!- userId: {}, roomId: {}", client.get("userId"), roomId);
            }

            log.debug("현재 플레이어 목록에서 해당 유저 제거 시도 중... - userId: {}, roomId: {}", client.get("userId"), roomId);
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
                log.debug("방에 남아있는 유저 중 첫 번째 유저를 방장으로 지정합니다.- roomId: {}", roomId);
                currentPlayers.get(0).setIsHost(true);
            }

            // 방에서 내보내기
            client.leaveRoom(key);  // room:1 형태의 키로 방 나가기
            log.debug("클라이언트를 소켓 room에서 내보냈습니다. - key: {}", key);

            // 방에 남은 유저가 없으면 rooms:와 waiting: 모두에서 방 삭제
            if (currentPlayers.isEmpty()) {
                log.debug("방에 남아있는 유저가 없어 redis에서 방 삭제를 시도 중 - roomKey: {}", roomKey);
                jsonRedisTemplate.delete(roomKey);  // rooms:room:1 삭제
                // waiting:에 해당 방이 있는지 확인 후 삭제
                if (Boolean.TRUE.equals(jsonRedisTemplate.hasKey(waitingKey))) {
                    log.debug("삭제하려는 방을 대기중인 방 목록에서 삭제 시도 중 - waitingKey: {}", waitingKey);
                    jsonRedisTemplate.delete(waitingKey);
                }
                log.info("방이 삭제되었습니다. - roomId: {}", roomId);
                updateResponseDto.setType("delete");
            } else {
                // 방 정보 업데이트 (rooms: 네임스페이스)
                log.debug("방에 유저가 남아있어 정보 업데이트를 진행합니다. - roomKey: {}", roomKey);
                room.setCurrentPlayers(currentPlayers);
                jsonRedisTemplate.opsForValue().set(roomKey, room);
                // 방 안에 남아있는 유저들에게 알림
                leaveRoomAlarmToAnotherClient(roomKey);
            }

            // 로비로 돌아가기
            lobbyService.joinLobby(client);

            return updateResponseDto;

        } catch (SocketException e) {
            log.error("방을 나가는데 실패했습니다.: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("방을 나가는 동안 예상하지 못한 오류가 발생했습니다.: {}", e.getMessage());
            throw new SocketException(LEAVE_ROOM_FAILED);
        }
    }

    public void leaveRoomSystemChat(String nickname, String key) {
        RoomChatResponseDto responseDto = RoomChatResponseDto.builder()
                .nickname("system")
                .message(nickname + " 님이 퇴장하셨습니다.")
                .build();

        namespace.getRoomOperations(key).sendEvent(ROOM_CHAT_SEND.getValue(), responseDto);
        log.info("room:chat:send 이벤트를 발생시켜 {}번 방에 {} 유저 퇴장을 시스템 메시지로 알림.", key, nickname);
    }

    public void leaveRoomAlarmToAnotherClient(String roomKey) throws SocketException {
        log.debug("방 안에 남아있는 유저들에게 업데이트 된 정보알림을 시도합니다. - eventType: room:leave:send, roomKey: {}", roomKey);
        Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room == null) {
            log.error("방 안에 있는 사람들에게 알리기 위한 key로 방을 찾을 수 없습니다.");
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

        String key = roomKey.substring(ROOM_LIST.length());
        namespace.getRoomOperations(key).sendEvent(LEAVE_ROOM_SEND.getValue(), responseDto);
        log.info("room:leave:send 이벤트를 발생시켜 {}번 방 정보를 해당 방 안에 남은 유저들에게 알림.", key);
    }

    // 유저가 특정 방에 있는지 확인
    public boolean isUserInRoom(SocketIOClient client, Long roomId) {
        log.debug("유저가 {}번 방에 있는지 확인하려는 시도 중", roomId);
        String key = ROOM_KEY_PREFIX + roomId;
        String roomKey = ROOM_LIST + key;
        Set<String> rooms = new HashSet<>(client.getAllRooms());

        log.debug("방 데이터를 가져오려고 시도 중 - roomKey: {}", roomKey);
        Object roomData = jsonRedisTemplate.opsForValue().get(roomKey);

        boolean userExists = rooms.contains(key);

        if (roomData instanceof LinkedHashMap<?, ?> map) {
            List<?> playersList = (List<?>) map.get("currentPlayers");

            if (playersList != null) {
                ObjectMapper mapper = new ObjectMapper();
                userExists = playersList.stream()
                        .map(player -> mapper.convertValue(player, RoomUser.class))
                        .anyMatch(user -> user.getUserId().equals(client.get("userId")));
            }
        }

        return userExists;
    }

    // 원활한 테스트를 위해 한 아이디로 한 방에 여러번 입장이 가능한 코드를 남겨둠.
//    public boolean isUserInRoom(SocketIOClient client, Long roomId) {
//        String key = ROOM_KEY_PREFIX + roomId;
//        String roomKey = ROOM_LIST + key;
//        Set<String> rooms = new HashSet<>(client.getAllRooms());
//
//        return rooms.contains(key);
//    }

    public RoomEnterResponseDto enterRoom(SocketIOClient client, Long roomId, Integer password) throws SocketException {
        String key = ROOM_KEY_PREFIX + roomId;
        String roomKey = ROOM_LIST + key;  // "rooms:room:1"
        log.debug("유저가 방 입장을 시도합니다. - userId: {}, room: {}", client.get("userId"), key);

        // Redis에서 rooms: 네임스페이스의 방 정보 조회
        Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room == null) {
            log.debug("입장하려는 방을 찾을 수 없습니다. - userId: {}, room: {}", client.get("userId"), key);
            throw new SocketException(ROOM_NOT_FOUND);
        }

        if(room.getCurrentPlayers().size() >= 6) {
            log.debug("정원이 다 찬 방에 입장을 시도했습니다. - userId: {}, room: {}", client.get("userId"), key);
            throw new SocketException(FULL_USER_IN_ROOM);
        } else if(room.getCurrentPlayers().size() <= 0) {
            log.debug("이미 없어진 방에 입장을 시도했습니다. - userId: {}, room: {}", client.get("userId"), key);
            throw new SocketException(ROOM_NOT_FOUND);
        }

        Integer enterId = 0;

        // 유저가 방에 있는지 확인
        if (!isUserInRoom(client, roomId)) {
            // 방이 private인 경우 비밀번호 체크
            if (room.getIsPrivate() && !room.getRoomPassword().equals(password)) {
                log.debug("비밀번호가 일치하지 않습니다. - userId: {}, room: {}", client.get("userId"), key);
                throw new SocketException(INVALID_ROOM_PASSWORD);
            }

            // 새로운 RoomUser 생성을 위한 유저 정보 가져오기
            String nickname = client.get("nickname");
            int userId = (Integer) client.get("userId");
            int profileImage = (Integer) client.get("profileImage");

            // 현재 방의 최대 enterId를 찾아서 +1
            int newEnterId = room.getCurrentPlayers().stream()
                    .mapToInt(RoomUser::getEnterId)
                    .max()
                    .orElse(0) + 1;  // 방이 비어있다면 1이 됨

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
            log.debug("reids에 새로운 유저 정보를 성공적으로 저장했습니다. - userId: {}, room: {}", client.get("userId"), key);

            // 입장하는 사용자를 이동시키기
            client.leaveRoom(LOBBY_ROOM);
            log.debug("클라이언트를 로비에서 퇴장시켰습니다. - userId: {}", (Integer) client.get("userId"));
            client.joinRoom(key);  // room:1 형태의 키로 방 입장
            log.debug("클라이언트가 방에 입장했습니다. - userId: {}, key: {}", (Integer) client.get("userId"), key);
        } else {
            log.debug("이미 해당 방에 있는 유저입니다. - userId: {}, room: {}", client.get("userId"), key);
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

    public void enterRoomSystemChat(String nickname, String key) {
        RoomChatResponseDto responseDto = RoomChatResponseDto.builder()
                .nickname("system")
                .message(nickname + " 님이 입장하셨습니다.")
                .build();

        namespace.getRoomOperations(key).sendEvent(ROOM_CHAT_SEND.getValue(), responseDto);
        log.info("room:chat:send 이벤트를 발생시켜 {}번 방에 {} 유저 입장을 시스템 메시지로 알림.", key, nickname);
    }

    public LobbyRoomListResponseDto findWaitingRoomList() {
        log.debug("대기중인 모든 방을 조회를 시도하는 중...");
        List<UpdateData> waitingRooms = new ArrayList<>();

        try {
            Set<String> waitingKeys = stringRedisTemplate.keys(WAITING_LIST + "*");
            log.debug("찾아낸 대기중인 방 리스트: {}", waitingKeys);

            for (String waitingKey : waitingKeys) {
                try {
                    String key = waitingKey.substring(WAITING_LIST.length());
                    String roomKey = ROOM_LIST + key;
//                    log.debug("Attempting to get room data for key: {}", roomKey);

                    Object roomData = jsonRedisTemplate.opsForValue().get(roomKey);
//                        log.debug("Raw room data: {}", roomData);
//                        log.debug("Raw room data class: {}", roomData != null ? roomData.getClass().getName() : "null");

                    if (roomData instanceof LinkedHashMap<?, ?> map) {
                        // Integer를 Long으로 안전하게 변환
                        Object roomIdObj = map.get("roomId");
                        Long roomId;
                        if (roomIdObj instanceof Integer) {
                            roomId = ((Integer) roomIdObj).longValue();
                        } else if (roomIdObj instanceof Long) {
                            roomId = (Long) roomIdObj;
                        } else {
                            roomId = Long.valueOf(roomIdObj.toString());
                        }

                        UpdateData updateData = UpdateData.builder()
                                .roomId(roomId)  // 변환된 Long 값 사용
                                .roomTitle((String) map.get("roomTitle"))
                                .isPrivate((Boolean) map.get("isPrivate"))
                                .currentPlayerNum(((List<?>) map.get("currentPlayers")).size())
                                .playing((Boolean) map.get("playing"))
                                .build();
                        waitingRooms.add(updateData);

                    }
                } catch (Exception e) {
                    log.error("{} 대기방의 정보를 찾던 중 예상하지 못한 오류가 발생했습니다. : {}", waitingKey, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("대기중인 방 리스트를 찾던 중 예상하지 못한 오류가 발생했습니다.: {}", e.getMessage(), e);
            throw e;
        }

        return LobbyRoomListResponseDto.builder()
                .rooms(waitingRooms)
                .totalPage(waitingRooms.size() / PAGE_SIZE)
                .build();
    }


}
