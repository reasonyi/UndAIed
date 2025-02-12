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
    private Integer userId;
    private Boolean isHost;
    private String nickname;
    private Integer profileImage;
}