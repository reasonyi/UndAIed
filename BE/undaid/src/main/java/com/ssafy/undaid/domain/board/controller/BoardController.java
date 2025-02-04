package com.ssafy.undaid.domain.board.controller;

import com.ssafy.undaid.domain.board.dto.request.BoardCreateRequestDto;
import com.ssafy.undaid.domain.board.dto.request.BoardUpdateRequestDto;
import com.ssafy.undaid.domain.board.dto.response.BoardCreateResponseDto;
import com.ssafy.undaid.domain.board.dto.response.BoardDetailResponseDto;
import com.ssafy.undaid.domain.board.dto.response.BoardListResponseDto;
import com.ssafy.undaid.domain.board.dto.response.BoardUpdateResponseDto;
import com.ssafy.undaid.domain.board.entity.Boards;
import com.ssafy.undaid.domain.board.service.BoardService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.ssafy.undaid.global.common.exception.ErrorCode.UNAUTHORIZED_TOKEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BoardController {

    private final JwtTokenProvider jwtTokenProvider;
    private final BoardService boardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/board")
    public ApiDataResponse<?> adminCreateBoard(@RequestBody BoardCreateRequestDto boardCreateRequestDto,
                                               @AuthenticationPrincipal Integer userId) {
        BoardCreateResponseDto result = boardService.createBoard(boardCreateRequestDto, userId);
        return new ApiDataResponse<>(HttpStatusCode.CREATED, result, "게시글 작성 완료");
    }



    //게시글 작성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/board")
    public ApiDataResponse<?> createBoard(@RequestBody BoardCreateRequestDto boardCreateRequestDto,
                                          @AuthenticationPrincipal Integer userId) {
        BoardCreateResponseDto result = boardService.createBoard(boardCreateRequestDto, userId);
        return new ApiDataResponse<>(HttpStatusCode.CREATED, result, "게시글 작성 완료");
    }

    // 게시글 목록 조회
    @GetMapping("/board")
    public ApiDataResponse<?> getAllBoards(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardListResponseDto> result = boardService.getAllBoards(pageable);
        return new ApiDataResponse<>(HttpStatusCode.OK, result,null);
    }

    // 특정 게시글 조회
    @GetMapping("/board/{boardId}")
    public ApiDataResponse<?> getBoard(@PathVariable("boardId") Integer boardId) {
        BoardDetailResponseDto result = boardService.getBoard(boardId);
        return new ApiDataResponse<>(HttpStatusCode.OK, result,null);
    }

    // 게시글 수정(관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/board/{boardId}")
    public ApiResponse adminUpdateBoard(@PathVariable("boardId") Integer boardId,
                                        @RequestBody BoardUpdateRequestDto boardUpdateRequestDto,
                                        @AuthenticationPrincipal Integer userId) {
        boardService.updateBoard(boardId, userId, boardUpdateRequestDto);
        return new ApiResponse(HttpStatusCode.OK.getIsSuccess(), HttpStatusCode.OK.getStatus(), "게시글 수정 완료");
    }

    //게시글 수정

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/board/{boardId}")
    public ApiDataResponse<BoardUpdateResponseDto> updateBoard(@PathVariable("boardId") Integer boardId,
                                                               @RequestBody BoardUpdateRequestDto boardUpdateRequestDto,
                                                               @AuthenticationPrincipal Integer userId) {
        BoardUpdateResponseDto result = boardService.updateBoard(boardId, userId, boardUpdateRequestDto);
        return new ApiDataResponse<>(HttpStatusCode.OK, result, "게시글 수정 성공");
    }


    //게시글 삭제(관리자) //모든 게시글 삭제 가능하도록

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/board/{boardId}")
    public ApiResponse adminDeleteBoard(@PathVariable("boardId") Integer boardId) {
        boardService.adminDeleteBoard(boardId);
        return new ApiResponse(HttpStatusCode.OK.getIsSuccess(), HttpStatusCode.OK.getStatus(), "게시글 삭제 완료");
    }

    //게시글 삭제

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/board/{boardId}")
    public ApiResponse deleteBoard(@PathVariable("boardId") Integer boardId,
                                   @AuthenticationPrincipal Integer userId) {
        boardService.deleteBoard(boardId, userId);
        return new ApiResponse(HttpStatusCode.OK.getIsSuccess(), HttpStatusCode.OK.getStatus(), "게시글 삭제 완료");
    }
}