package com.ssafy.undaid.domain.user.dto;

import com.ssafy.undaid.global.common.response.HttpStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class TokenValidationDto {
    private HttpStatusCode httpStatusCode;
    private String message;
    private UserLoginResponseDto userLoginResponseDto;
}
