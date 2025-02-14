package com.ssafy.undaied.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequestDto {
    private Boolean sex;
    private Integer profileImage;
    private Integer avatar;
    private Integer age;
    private String nickname;
}