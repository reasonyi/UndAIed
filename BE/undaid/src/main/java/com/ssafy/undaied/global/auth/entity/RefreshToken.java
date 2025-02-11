package com.ssafy.undaied.global.auth.entity;

import org.springframework.data.annotation.Id; // 수정
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter @Setter
@RedisHash(value = "refreshToken", timeToLive = 1209600) // 2주
public class RefreshToken {
    @Id
    private String id;
    private String refreshToken;

    public RefreshToken(String id, String refreshToken) {
        this.id = id;
        this.refreshToken = refreshToken;
    }
}