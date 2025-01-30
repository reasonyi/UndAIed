package com.ssafy.undaid.domain.friend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestListResponseDto {
    private Integer friendshipId;
    private Integer friendId;
    private String friendNickname;
}
