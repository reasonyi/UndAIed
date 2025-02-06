package com.ssafy.undaied.socket.room.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private int enterId;
    private boolean isHost;
    private String nickname;
}