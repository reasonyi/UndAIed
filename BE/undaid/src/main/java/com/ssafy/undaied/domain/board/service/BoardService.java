package com.ssafy.undaied.domain.board.service;

import com.ssafy.undaied.domain.board.dto.request.BoardCreateRequestDto;
import com.ssafy.undaied.domain.board.dto.request.BoardUpdateRequestDto;
import com.ssafy.undaied.domain.board.dto.response.BoardCreateResponseDto;
import com.ssafy.undaied.domain.board.dto.response.BoardDetailResponseDto;
import com.ssafy.undaied.domain.board.dto.response.BoardListResponseDto;
import com.ssafy.undaied.domain.board.dto.response.BoardUpdateResponseDto;
import com.ssafy.undaied.domain.board.entity.Boards;
import com.ssafy.undaied.domain.board.entity.repository.BoardRepository;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.ssafy.undaied.global.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    


    // 게시글 생성
    @Transactional
    public BoardCreateResponseDto createBoard(BoardCreateRequestDto boardCreateRequestDto, int userId) {
        // writerId를 사용해 Users 엔티티를 조회
        Users writer = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));

        // Board 엔티티 생성
        Boards boards = Boards.builder()
                .title(boardCreateRequestDto.getTitle())
                .content(boardCreateRequestDto.getContent())
                .category(boardCreateRequestDto.getCategory())
                .writer(writer) // Users 객체 설정
                .build();

        Boards savedBoards =boardRepository.save(boards);

        return BoardCreateResponseDto.builder()
                .boardId(savedBoards.getBoardId())
                .title(savedBoards.getTitle())
                .content(savedBoards.getContent())
                .writerNickname(savedBoards.getWriter().getNickname())
                .category(savedBoards.getCategory())
                .createdAt(savedBoards.getCreatedAt())
                .updatedAt(savedBoards.getUpdatedAt())
                .build();

    }

    // 게시글 리스트 조회
    @Transactional(readOnly = true)
    public Page<BoardListResponseDto> getAllBoards(Byte category, Pageable pageable) {
        if (category == null) {
            return boardRepository.findAll(pageable)
                    .map(this::toDto);
        } else {
            return boardRepository.findByCategory(category, pageable)
                    .map(this::toDto);
        }
    }

    // 게시글 상세 조회
    @Transactional
    public BoardDetailResponseDto getBoard(Integer boardId) {

        // 게시글 조회
        Boards boards = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(BOARD_NOT_FOUND));

        // 조회수 증가
        boards.increaseViewCount();

        // Board 객체를 DTO로 변환하여 반환
        return toDetailDto(boards);
    }

    //게시글 수정
    @Transactional
    public BoardUpdateResponseDto updateBoard(Integer boardId, Integer userId, BoardUpdateRequestDto boardUpdateRequestDto){

        Boards boards = boardRepository.findById(boardId).orElseThrow(() -> new BaseException(BOARD_NOT_FOUND));

        // 작성자와 요청한 사용자 비교
        if (!Objects.equals(boards.getWriter().getUserId(), userId)) {
            throw new BaseException(BOARD_NOT_AUTHORIZED);
        }

        boards.update(boardUpdateRequestDto.getTitle(), boardUpdateRequestDto.getContent());

        // 수정된 데이터 반환
        return BoardUpdateResponseDto.builder()
                .title(boards.getTitle())
                .content(boards.getContent())
                .writerNickname(boards.getWriter().getNickname())
                .category(boards.getCategory())
                .createdAt(boards.getCreatedAt())
                .updatedAt(boards.getUpdatedAt())
                .build();
    }

    //관리자 게시글 삭제(모든 게시글 삭제 허용)
    @Transactional
    public void adminDeleteBoard(Integer boardId) {

        // boardId로 작성자 찾기 위해 객체 생성
        Boards boards = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(BOARD_NOT_FOUND));

        boardRepository.delete(boards);
    }

    //게시글 삭제
    @Transactional
    public void deleteBoard(Integer boardId, Integer userId) {

        // boardId로 작성자 찾기 위해 객체 생성
        Boards boards = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(BOARD_NOT_FOUND));

        // 작성자와 요청한 사용자 비교
        if (!Objects.equals(boards.getWriter().getUserId(), userId)) {
            throw new BaseException(BOARD_NOT_AUTHORIZED);
        }

        boardRepository.delete(boards);
    }

    // Board를 BoardDetailResponseDto로 변환
    private BoardDetailResponseDto toDetailDto(Boards boards) {
        return BoardDetailResponseDto.builder()
                .title(boards.getTitle())                       // 게시글 제목
                .writerNickname(boards.getWriter().getNickname())     // 작성자 닉네임
                .category(boards.getCategory())                // 카테고리
                .content(boards.getContent())                  // 내용
                .viewCnt(boards.getViewCnt())                  // 조회수
                .createdAt(boards.getCreatedAt())              // 생성일
                .updatedAt(boards.getUpdatedAt())              // 수정일
                .build();
    }


    // Board를 BoardListResponseDto로 변환
    private BoardListResponseDto toDto(Boards boards) {
        return BoardListResponseDto.builder()
                .boardId(boards.getBoardId())                      // 게시글 ID
                .title(boards.getTitle())                          // 제목
                .writerNickname(boards.getWriter().getNickname())        // 작성자 닉네임
                .category(boards.getCategory())                    // 카테고리
                .viewCnt(boards.getViewCnt())                      // 조회수
                .createdAt(boards.getCreatedAt())                  // 생성 시간
                .updatedAt(boards.getUpdatedAt())                  // 수정 시간
                .build();
    }
}