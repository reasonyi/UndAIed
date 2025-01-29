package com.ssafy.undaid.domain.friend.service;

import com.ssafy.undaid.domain.friend.dto.request.CreateFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.request.DeleteFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.request.UpdateFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.response.FriendResponseDto;
import com.ssafy.undaid.domain.friend.dto.response.UpdateFriendResponseDto;
import com.ssafy.undaid.domain.friend.entity.Friends;
import com.ssafy.undaid.domain.friend.entity.FriendshipStatus;
import com.ssafy.undaid.domain.friend.entity.repository.FriendRepository;
import com.ssafy.undaid.domain.user.entity.Users;
import com.ssafy.undaid.domain.user.entity.repository.UserRepository;
import com.ssafy.undaid.domain.user.service.UserService;
import com.ssafy.undaid.global.common.exception.BaseException;
import com.ssafy.undaid.global.common.exception.ErrorCode;
import com.ssafy.undaid.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public List<FriendResponseDto> getFriendsList(int userId) {
        return friendRepository.findByUserId(userId);
    }

    public List<FriendResponseDto> getFriendRequestsList(int userId) {
        return friendRepository.findPendingByUserId(userId);
    }

    public void createFriend(HttpServletRequest request, CreateFriendRequestDto createFriendRequestDto) {
        String token = jwtTokenProvider.resolveToken(request);
        if(token == null) throw new BaseException(ErrorCode.UNAUTHORIZED_TOKEN);

        int userId = jwtTokenProvider.getUserIdFromToken(token);
        Users user = userService.getUserById(userId);   // 친구 요청을 보낸 사용자
        Users friendUser = userService.getUserByNickname(createFriendRequestDto.getNickname()); // 친구 요청을 받은 사용자

        Friends friendship = friendRepository.findByUserIdAndFriendId(user.getUserId(), friendUser.getUserId());
        if ( friendship != null) {
            // 차단인 경우
            if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
                throw new BaseException(ErrorCode.USER_NOT_FOUND);
            } else {
                // 이미 친구이거나 친구 요청을 보낸 경우
                throw new BaseException(ErrorCode.FRIENDSHIP_ALREADY_REQUESTED);
            }
        }

        Friends friend = Friends.builder()
                .status(FriendshipStatus.PENDING)
                .user(user)
                .friendUser(friendUser)
                .build();

        friendRepository.save(friend);
    }

    public UpdateFriendResponseDto updateFriendStatus(UpdateFriendRequestDto updateFriendRequestDto) {
        Friends friend = friendRepository.findByUserIdAndFriendId(updateFriendRequestDto.getUserId(), updateFriendRequestDto.getFriendId());

        friend.updateStatus(updateFriendRequestDto.getStatus());

        Friends updatedFriend = friendRepository.save(friend);

        return UpdateFriendResponseDto.builder()
                .status(updatedFriend.getStatus())
                .updatedAt(updatedFriend.getUpdatedAt())
                .userId(updatedFriend.getUser().getUserId())
                .friendId(updatedFriend.getFriendUser().getUserId())
                .build();
    }

    public void deleteFriend(DeleteFriendRequestDto deleteFriendRequestDto) {
        Friends friend = friendRepository.findByUserIdAndFriendId(deleteFriendRequestDto.getUserId(), deleteFriendRequestDto.getFriendId());
        friendRepository.delete(friend);

    }

}
