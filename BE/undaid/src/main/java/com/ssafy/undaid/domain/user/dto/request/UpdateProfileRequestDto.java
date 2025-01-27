package com.ssafy.undaid.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequestDto {
    private int sex;
    private int profileImage;
    private int avatar;
    private int age;
}