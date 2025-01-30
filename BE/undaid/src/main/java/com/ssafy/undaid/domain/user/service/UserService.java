package com.ssafy.undaid.domain.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.undaid.domain.game.entity.GameParticipants;
import com.ssafy.undaid.domain.game.entity.Games;
import com.ssafy.undaid.domain.game.entity.respository.GameParticipantsRepository;
import com.ssafy.undaid.domain.user.dto.request.UpdateProfileRequestDto;
import com.ssafy.undaid.domain.user.dto.response.GameThumbnailResponseDto;
import com.ssafy.undaid.domain.user.dto.response.TokenValidationDto;
import com.ssafy.undaid.domain.user.dto.response.UserLoginResponseDto;
import com.ssafy.undaid.domain.user.dto.response.UserProfileResponseDto;
import com.ssafy.undaid.domain.user.entity.Users;
import com.ssafy.undaid.domain.user.entity.repository.UserRepository;
import com.ssafy.undaid.global.common.exception.BaseException;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import com.ssafy.undaid.global.oauth.service.OAuth2Service;
import com.ssafy.undaid.global.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ssafy.undaid.global.common.exception.ErrorCode.*;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;
    private final GameParticipantsRepository gameParticipantsRepository;
    private final OAuth2Service oAuth2Service;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public Users createUser(JsonNode node, String email) {
        Users user = Users.builder()
                .email(email)
                .nickname(email.split("@")[0])
                .provider(oAuth2Service.checkProvider(node.get("iss").asText()))
                .providerId(node.get("sub").asText())
                .build();

        userRepository.save(user);

        return user;
    }

    // 로그인
    public String login(Users user) {
        return jwtTokenProvider.createToken(user.getEmail(), user.getRoleType(), user.getUserId());
    }

    public TokenValidationDto tokenValidate(String token) {
        JsonNode node = oAuth2Service.extractJsonNode(token);

        if (node == null || !node.has("email")) {
            throw new BaseException(TOKEN_VALIDATION_FAILED);
        }

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

    @Transactional
    public UserProfileResponseDto updateProfile(UpdateProfileRequestDto requestDto, int userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));

        user.updateProfile(requestDto.getProfileImage(), requestDto.getAvatar(), requestDto.isSex(), requestDto.getAge(), requestDto.getNickname());

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