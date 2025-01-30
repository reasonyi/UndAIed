package com.ssafy.undaid.domain.friend.dto.response;

import com.ssafy.undaid.domain.friend.entity.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendUpdateResponseDto {
    private FriendshipStatus status;
    private LocalDateTime updatedAt;
    private Integer userId;
    private Integer friendId;
}
