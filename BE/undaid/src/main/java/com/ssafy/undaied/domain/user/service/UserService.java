package com.ssafy.undaied.domain.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.undaied.domain.game.entity.GameParticipants;
import com.ssafy.undaied.domain.game.entity.Games;
import com.ssafy.undaied.domain.game.entity.respository.GameParticipantsRepository;
import com.ssafy.undaied.domain.user.dto.request.UpdateProfileRequestDto;
import com.ssafy.undaied.domain.user.dto.response.GameThumbnailResponseDto;
import com.ssafy.undaied.domain.user.dto.response.TokenValidationDto;
import com.ssafy.undaied.domain.user.dto.response.UserLoginResponseDto;
import com.ssafy.undaied.domain.user.dto.response.UserProfileResponseDto;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.response.HttpStatusCode;
import com.ssafy.undaied.global.auth.service.OAuth2Service;
import com.ssafy.undaied.global.auth.token.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.ssafy.undaied.global.common.exception.ErrorCode.*;


@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;
    private final GameParticipantsRepository gameParticipantsRepository;
    private final OAuth2Service oAuth2Service;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public Users createUser(JsonNode node, String email) {
        String hashedProviderId = BCrypt.hashpw(node.get("sub").asText(), BCrypt.gensalt());    // BCrypt

        Users user = Users.builder()
                .email(email)
                .nickname(email.split("@")[0])
                .provider(oAuth2Service.checkProvider(node.get("iss").asText()))
                .providerId(hashedProviderId)
                .build();

        userRepository.save(user);

        return user;
    }

    // 로그인
    public String login(Users user) {
        return jwtTokenProvider.createToken(user.getEmail(), user.getRoleType(), user.getUserId());
    }

    public TokenValidationDto tokenValidate(String token) {
        JsonNode node = oAuth2Service.extractJsonNode(token);   // 필수 필드 검증 여기서 하고있음

        String email = node.get("email").asText();

        TokenValidationDto tokenValidationDto = new TokenValidationDto();
        tokenValidationDto.setHttpStatusCode(HttpStatusCode.OK);
        tokenValidationDto.setMessage("로그인 성공");

        if(!userRepository.existsByEmail(email)) {
            // 회원 가입 후 로그인 진행
            createUser(node, email);
            tokenValidationDto.setHttpStatusCode(HttpStatusCode.CREATED);
            tokenValidationDto.setMessage("회원가입 성공");
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        if (user.getIsDeleted()) {
            throw new BaseException(DELETED_USER);  // 유저가 탈퇴한 경우 다시 회원가입 및 로그인 할 수 없음.
        }

        String jwtToken = login(user);
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new BaseException(JWT_CREATION_FAILED);
        }

        UserLoginResponseDto userLoginResponseDto = UserLoginResponseDto.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .totalWin(user.getTotalWin())
                .totalLose(user.getTotalLose())
                .profileImage(user.getProfileImage())
                .build();

        tokenValidationDto.setUserLoginResponseDto(userLoginResponseDto);

        return tokenValidationDto;
    }

    public UserProfileResponseDto getUserProfile(int userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));

        List<GameParticipants> participants = gameParticipantsRepository.findDistinctByUserUserId(userId);
        List<Games> games = null;

        if (!participants.isEmpty()) {
            games = participants
                    .stream()
                    .map(GameParticipants::getGame)
                    .toList();
        }

        List<GameThumbnailResponseDto> dtoList = games != null ?
                games.stream()
                        .map(game -> GameThumbnailResponseDto.builder()
                                .gameId(game.getGameId())
                                .roomTitle(game.getRoomTitle())
                                .startedAt(game.getStartedAt())
                                .playTime(game.getPlayTime())
                                .build())
                        .toList()
                : Collections.emptyList();

        return UserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .avatar(user.getAvatar())
                .sex(user.getSex())
                .age(user.getAge())
                .totalWin(user.getTotalWin())
                .totalLose(user.getTotalLose())
                .game(dtoList)
                .build();
    }

    // =============== 친구 추가를 위해 추가할 내용
    public Users getUserByNickname(String nickname) {
        Users user = userRepository.findByNickname(nickname)
                .orElseThrow(()-> new BaseException(USER_NOT_FOUND));
        return user;
    }

    public Users getUserById(int userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(()-> new BaseException(USER_NOT_FOUND));
        return user;
    }

    public void nicknameValidation(String nickname) {
        if(nickname.equalsIgnoreCase("system") || nickname.equalsIgnoreCase("event")) {
            log.error("시스템 사용 닉네임으로 변경 시도");
            throw new BaseException(NOT_ALLOW_NICKNAME);
        }
        if(nickname.matches(".*[^a-zA-Z0-9가-힣].*")) {
            log.error("닉네임에 특수문자 사용 시도");
            throw new BaseException(NOT_ALLOW_SPECIAL_CHARACTERS);
        }
    }

    @Transactional
    public UserProfileResponseDto updateProfile(UpdateProfileRequestDto requestDto, int userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));

        if(requestDto.getNickname() != null) {
            if(!requestDto.getNickname().equals(user.getNickname())) {
                if(userRepository.existsByNickname(requestDto.getNickname())) {
                    log.error("중복되는 닉네임으로 변경 요청");
                    throw new BaseException(ALREADY_NICKNAME_EXISTS);
                }
                nicknameValidation(requestDto.getNickname());
            }
        }

        System.out.println("현재 유저 데이터: " +
                "프로필 이미지: "+user.getProfileImage()+", 아바타 이미지: "+ user.getAvatar()+", 성별: "+user.getSex()+", 닉네임 {}" +user.getNickname());

        System.out.println("바꿀 유저 데이터: " +
                "프로필 이미지: "+requestDto.getProfileImage()+", 아바타 이미지: "+ requestDto.getAvatar()+", 성별: "+requestDto.getSex()+", 닉네임 {}" +requestDto.getNickname());

        user.updateProfile(requestDto.getProfileImage(), requestDto.getAvatar(), requestDto.getSex(), requestDto.getAge(), requestDto.getNickname());

        return getUserProfile(userId);
    }

    // 로그아웃 시 리프레시 토큰 삭제
    @Transactional
    public void signout(String refreshToken) {
        // RefreshToken 삭제만 수행
//        redisTemplate.delete(refreshToken);
    }

    // 회원 탈퇴
    public void deleteUser(int userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        signout("로그아웃 로직 만들겠습니다.");
        user.deleteUser();
    }

}