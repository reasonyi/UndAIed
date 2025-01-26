package com.ssafy.undaid.domain.friend.service;

import com.ssafy.undaid.domain.friend.dto.request.UpdateFriendRequestDto;
import com.ssafy.undaid.domain.friend.dto.response.FriendResponseDto;
import com.ssafy.undaid.domain.friend.dto.response.UpdateFriendResponseDto;
import com.ssafy.undaid.domain.friend.entity.Friends;
import com.ssafy.undaid.domain.friend.entity.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;

    public List<FriendResponseDto> getFriendsList(int userId) {
        return friendRepository.findByUserId(userId);
    }

    public List<FriendResponseDto> getFriendRequestsList(int userId) {
        return friendRepository.findPendingByUserId(userId);
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

}
