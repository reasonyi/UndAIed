package com.ssafy.undaid.domain.friend.dto.response;

import com.ssafy.undaid.domain.friend.entity.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponseDto {
    private FriendshipStatus status;
    private int userId;
    private int friendId;
}
