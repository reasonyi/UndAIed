package com.ssafy.undaied.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponseDto {
    private String token;
    private String email;
    private String nickname;
    private int totalWin;
    private int totalLose;
    private int profileImage;
}
