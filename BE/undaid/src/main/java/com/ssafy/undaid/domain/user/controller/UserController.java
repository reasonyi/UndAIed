package com.ssafy.undaid.domain.user.controller;

import com.ssafy.undaid.domain.user.dto.TokenValidationDto;
import com.ssafy.undaid.domain.user.service.UserService;
import com.ssafy.undaid.global.common.response.ApiDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    // 회원가입 또는 로그인
    @PostMapping
    public ApiDataResponse<?> signUpOrSignIn(@RequestBody Map<String, String> token) {
        TokenValidationDto result = userService.tokenValidate(token.get("token"));
        return new ApiDataResponse<>(result.getHttpStatusCode(), result.getUserLoginResponseDto(), result.getMessage());
    }


}
