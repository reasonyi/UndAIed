package com.ssafy.undaid.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    @NotNull
    private String email;
    @NotNull
    private String nickname;
    private Boolean sex;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ProviderType provider;
    @NotNull
    private String providerId;
    @NotNull
    private Integer profileImage;
    @NotNull
    private Integer avatar;
    private Integer age;
    @NotNull
    private Boolean isDeleted;
    @NotNull
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private Integer totalWin;
    private Integer totalLose;

    @Builder
    public Users(
            String email,
            String nickname,
            ProviderType provider,
            String providerId
    ) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = 1;
        this.avatar = 1;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalWin = 0;
        this.totalLose = 0;
    }
}