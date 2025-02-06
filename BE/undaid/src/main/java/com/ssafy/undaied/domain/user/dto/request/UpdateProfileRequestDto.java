package com.ssafy.undaied.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequestDto {
    private boolean sex;
    private int profileImage;
    private int avatar;
    private int age;
    private String nickname;
}