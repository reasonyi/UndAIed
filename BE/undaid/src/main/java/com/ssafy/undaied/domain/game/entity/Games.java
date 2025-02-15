package com.ssafy.undaied.domain.game.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "games")
public class Games {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private int gameId;

    @Column(length = 50)
    @NonNull
    private String roomTitle;

    @NonNull
    private LocalDateTime startedAt;

    @NonNull
    private LocalDateTime endedAt;

    @Column(length = 15)
    @NonNull
    private String playTime;

    @NonNull
    private Boolean humanWin;
}