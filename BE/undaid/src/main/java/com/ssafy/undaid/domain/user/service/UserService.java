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

        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BaseException(USER_NOT_FOUND);
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

        // age와 sex가 null일 경우 기본값 처리
        Integer age = user.getAge() != null ? user.getAge() : 0; // 기본값 0으로 설정
        boolean sex = user.getSex() != null ? user.getSex() : true; // 기본값 "Unknown"으로 설정

        return UserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .avatar(user.getAvatar())
                .sex(sex)
                .age(age)
                .totalWin(user.getTotalWin())
                .totalLose(user.getTotalLose())
                .game(dtoList)
                .build();
    }

//    public UpdateProfileRequestDto updateProfile(UpdateProfileRequestDto updateProfileRequestDto, int userId) {
//
//    }



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


}