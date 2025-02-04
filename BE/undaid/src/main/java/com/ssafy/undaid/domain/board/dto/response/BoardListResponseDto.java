package com.ssafy.undaid.domain.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class BoardListResponseDto {
    Integer boardId;
    String writerNickname;
    String title;
    Byte category;
    Integer viewCnt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
