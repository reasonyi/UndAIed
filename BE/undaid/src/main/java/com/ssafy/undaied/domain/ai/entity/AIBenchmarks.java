package com.ssafy.undaied.domain.ai.entity;

import com.ssafy.undaied.domain.game.entity.Games;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ai_benchmarks")
public class AIBenchmarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aiBenchmarksId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id", nullable = false)
    private Games game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ai_id", nullable = true)
    private AIs ai;

    @NotNull
    private Integer deadRound;

}
