package com.ssafy.undaid.domain.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.undaid.domain.user.dto.TokenValidationDto;
import com.ssafy.undaid.domain.user.dto.UserLoginResponseDto;
import com.ssafy.undaid.domain.user.entity.Users;
import com.ssafy.undaid.domain.user.entity.repository.UserRepository;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import com.ssafy.undaid.global.oauth.service.OAuth2Service;
import com.ssafy.undaid.global.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;
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
        String jwtToken = login(user);

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
}