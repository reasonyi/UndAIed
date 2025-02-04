package com.ssafy.undaied.domain.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ais")
public class AIs {

    @Id
    private int aiId;

    @Column(length = 50)
    @NonNull
    private String aiName;

    @NonNull
    private LocalDateTime createdAt;
}