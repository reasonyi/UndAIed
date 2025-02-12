package com.ssafy.undaied.socket.room.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RoomUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer enterId;
    private Boolean isHost;
    private String nickname;
    //테스트 위해 일단 임의로 추가
    private Integer userId;
    private Integer profileImage;
}