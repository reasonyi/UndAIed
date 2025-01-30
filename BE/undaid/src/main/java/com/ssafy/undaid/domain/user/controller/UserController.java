package com.ssafy.undaid.domain.user.controller;

import com.ssafy.undaid.domain.user.dto.request.UpdateProfileRequestDto;
import com.ssafy.undaid.domain.user.dto.response.TokenValidationDto;
import com.ssafy.undaid.domain.user.dto.response.UserProfileResponseDto;
import com.ssafy.undaid.domain.user.service.UserService;
import com.ssafy.undaid.global.common.exception.BaseException;
import com.ssafy.undaid.global.common.response.ApiDataResponse;
import com.ssafy.undaid.global.common.response.ApiResponse;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import com.ssafy.undaid.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.ssafy.undaid.global.common.exception.ErrorCode.UNAUTHORIZED_TOKEN;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 또는 로그인
    @PostMapping
    public ApiDataResponse<?> signUpOrSignIn(@RequestBody Map<String, String> token) {
        TokenValidationDto result = userService.tokenValidate(token.get("token"));
        return ApiDataResponse.of(result.getHttpStatusCode(), result.getUserLoginResponseDto(), result.getMessage());
    }

    // 회원 프로필 조회
    @GetMapping
    public ApiDataResponse<?> getUserInfo() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserProfileResponseDto responseDto = userService.getUserProfile(userId);
        return ApiDataResponse.of(HttpStatusCode.OK, responseDto, "프로필 조회 성공");
    }

    // 회원 프로필 업데이트
    @PatchMapping("/profile")
    public ApiResponse updateProfile(@RequestBody UpdateProfileRequestDto updateProfileRequestDto) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserProfileResponseDto responseDto = userService.updateProfile(updateProfileRequestDto, userId);
        return ApiDataResponse.of(HttpStatusCode.OK, responseDto, "프로필이 수정되었습니다.");
    }

}
