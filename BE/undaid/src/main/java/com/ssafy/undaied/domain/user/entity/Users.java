package com.ssafy.undaied.domain.user.entity;

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
    @Column(length = 50)
    private String email;
    @NotNull
    @Column(length = 50)
    private String nickname;
    private Boolean sex;

    @NotNull
    @Enumerated(EnumType.STRING)
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
    @NotNull
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

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
        this.roleType = RoleType.USER;
        this.sex = true;
        this.age = 0;
    }

    public void updateProfile(Integer profileImage, Integer avatar, Boolean sex, Integer age, String nickname) {
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }
        if (sex != null) {
            this.sex = sex;
        }
        if (age != null) {
            this.age = age;
        }
        if(nickname != null) {
            this.nickname = nickname;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void win() {
        totalWin++;
    }

    public void lose() {
        totalLose++;
    }

    public void deleteUser() {
        isDeleted = true;
    }
}