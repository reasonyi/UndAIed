package com.ssafy.undaid.domain.comment.controller;

import com.ssafy.undaid.domain.board.dto.response.BoardCreateResponseDto;
import com.ssafy.undaid.domain.comment.dto.request.CommentCreateRequestDto;
import com.ssafy.undaid.domain.comment.dto.request.CommentUpdateRequestDto;
import com.ssafy.undaid.domain.comment.dto.response.CommentCreateResponseDto;
import com.ssafy.undaid.domain.comment.dto.response.CommentListResponseDto;
import com.ssafy.undaid.domain.comment.service.CommentService;
import com.ssafy.undaid.global.common.exception.BaseException;
import com.ssafy.undaid.global.common.response.ApiDataResponse;
import com.ssafy.undaid.global.common.response.ApiResponse;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import com.ssafy.undaid.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ssafy.undaid.global.common.exception.ErrorCode.UNAUTHORIZED_TOKEN;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    //댓글 목록 조회
    @GetMapping("/{boardId}/comment")
    public ApiDataResponse<Page<CommentListResponseDto>> getComments(
            @PathVariable Integer boardId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CommentListResponseDto> comments = commentService.getComments(boardId, pageable);
        return new ApiDataResponse<>(HttpStatusCode.OK, comments, null);
    }

    // 댓글 작성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardId}/comment")
    public ApiDataResponse<?> createComments(@PathVariable int boardId,
                                             @RequestBody CommentCreateRequestDto commentCreateRequestDto,
                                             @AuthenticationPrincipal Integer userId) {

        CommentCreateResponseDto result = commentService.createComments(boardId, commentCreateRequestDto, userId);
        return new ApiDataResponse<>(HttpStatusCode.CREATED, result, "댓글 작성 완료");
    }

    // 댓글 수정
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{commentId}")
    public ApiResponse updateComment(@PathVariable("commentId") Integer commentId,
                                     @RequestBody CommentUpdateRequestDto commentUpdateRequestDto,
                                     @AuthenticationPrincipal Integer userId) {

        commentService.updateComment(commentId, userId, commentUpdateRequestDto);
        return new ApiResponse(HttpStatusCode.OK.getIsSuccess(), HttpStatusCode.OK.getStatus(), "댓글 수정 완료");
    }


    // 댓글 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{commentId}")
    public ApiResponse deleteComment(@PathVariable("commentId") Integer commentId,
                                     @AuthenticationPrincipal Integer userId) {

        commentService.deleteComment(commentId, userId);
        return new ApiResponse(HttpStatusCode.OK.getIsSuccess(), HttpStatusCode.OK.getStatus(), "댓글 삭제 완료");
    }

    //관리자 댓글 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{commentId}")
    public ApiResponse adminDeleteComment(@PathVariable("commentId") Integer commentId,
                                          @AuthenticationPrincipal Integer userId) {

        commentService.adminDeleteComment(commentId, userId);
        return new ApiResponse(HttpStatusCode.OK.getIsSuccess(), HttpStatusCode.OK.getStatus(), "댓글 삭제 완료");
    }
}