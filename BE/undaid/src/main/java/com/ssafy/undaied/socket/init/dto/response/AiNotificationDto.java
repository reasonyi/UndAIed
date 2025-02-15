package com.ssafy.undaied.socket.init.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class AiNotificationDto {
    private List<AiInfo> selectedAIs;
}
