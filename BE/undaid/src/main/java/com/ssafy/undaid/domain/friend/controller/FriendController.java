package com.ssafy.undaid.domain.friend.controller;

import com.ssafy.undaid.domain.friend.dto.response.FriendResponseDto;
import com.ssafy.undaid.domain.friend.service.FriendService;
import com.ssafy.undaid.global.common.response.ApiDataResponse;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friend")
public class FriendController {

    private final FriendService friendService;

    // 사용자의 친구 목록 가져오기
    @GetMapping("/")
    public ApiDataResponse<List<FriendResponseDto>> getFriends(
            @RequestParam("userId") int userId) {
        List<FriendResponseDto> friendsList = friendService.getFriendsList(userId);
        return ApiDataResponse.of(HttpStatusCode.OK, friendsList, "친구 목록 조회 성공");
    }

    // 친구 요청 목록 가져오기
    @GetMapping({"/request"})
    public ApiDataResponse<List<FriendResponseDto>> getFriendRequestsList(
            @RequestParam int userId) {
        List<FriendResponseDto> friendRequestsList = friendService.getFriendRequestsList(userId);
        return ApiDataResponse.of(HttpStatusCode.CREATED, friendRequestsList, "친구 요청 목록 조회 성공");
    }
}
