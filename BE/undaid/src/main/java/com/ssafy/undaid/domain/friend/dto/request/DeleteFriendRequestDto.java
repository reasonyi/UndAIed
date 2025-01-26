package com.ssafy.undaid.domain.friend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFriendRequestDto {
    int userId;
    int friendId;
}
