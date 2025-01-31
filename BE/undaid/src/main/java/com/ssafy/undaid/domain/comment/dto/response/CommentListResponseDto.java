package com.ssafy.undaid.domain.comment.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentListResponseDto {

    private int commentId;
    private String commenterNickname;
    private String commentContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
