package com.ssafy.undaid.domain.board.dto.response;

import com.ssafy.undaid.domain.user.entity.Users;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BoardCreateResponseDto {
    private int boardId;
    private String title;
    private String content;
    private Byte category;
    private String writerNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
