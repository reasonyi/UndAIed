package com.ssafy.undaid.domain.friend.controller;

import com.ssafy.undaid.domain.friend.dto.request.CreateFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.request.DeleteFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.request.UpdateFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.response.FriendRequestListResponseDto;
import com.ssafy.undaid.domain.friend.dto.response.FriendResponseDto;
import com.ssafy.undaid.domain.friend.dto.response.UpdateFriendResponseDto;
import com.ssafy.undaid.domain.friend.service.FriendService;
import com.ssafy.undaid.global.common.response.ApiDataResponse;
import com.ssafy.undaid.global.common.response.ApiResponse;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public ApiDataResponse<List<FriendRequestListResponseDto>> getFriendRequestsList(
            HttpServletRequest request) {
        List<FriendRequestListResponseDto> responseDtoList = friendService.getFriendRequestsList(request);
        return ApiDataResponse.of(HttpStatusCode.OK, responseDtoList, "친구 요청 목록 조회 성공");
    }

    // 친구 요청
    @PostMapping("/")
    public ApiResponse createFriend(HttpServletRequest request, @RequestBody CreateFriendRequestDto createFriendRequestDto) {
        friendService.createFriend(request, createFriendRequestDto);
        return ApiResponse.of(HttpStatusCode.OK, "친구 요청을 보냈습니다.");
    }

    // 친구 요청 변경 (수락/거절)
    @PatchMapping("/update-status")
    public ApiDataResponse<UpdateFriendResponseDto> updateFriendStatus(
            @RequestBody UpdateFriendRequestDto updateFriendRequestDto) {
        UpdateFriendResponseDto responseDto = friendService.updateFriendStatus(updateFriendRequestDto);
        return ApiDataResponse.of(HttpStatusCode.OK, responseDto, "친구 상태가 변경되었습니다.");
    }

    // 친구 요청 삭제
    @DeleteMapping("/delete")
    public ApiResponse deleteFriend(
            @RequestBody DeleteFriendRequestDto deleteFriendRequestDto) {
        friendService.deleteFriend(deleteFriendRequestDto);
        return  ApiResponse.of(HttpStatusCode.OK, "친구 삭제가 완료되었습니다.");
    }
}
