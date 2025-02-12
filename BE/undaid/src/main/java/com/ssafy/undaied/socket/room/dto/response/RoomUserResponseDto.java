package com.ssafy.undaied.socket.room.dto.response;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class RoomUserResponseDto {
    private Integer enterId;
    private Boolean isHost;
    private String nickname;
    private Integer profileImage;
}