package com.ssafy.undaied.socket.room.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long roomId;
    private String roomTitle;
    private Boolean isPrivate;
    private Integer roomPassword;
    private Boolean playing;
    private List<RoomUser> currentPlayers;
}