package com.ssafy.undaid.domain.game.controller;

import com.ssafy.undaid.domain.game.dto.response.GameDetailResponseDto;
import com.ssafy.undaid.domain.game.service.GameService;
import com.ssafy.undaid.global.common.response.ApiDataResponse;
import com.ssafy.undaid.global.common.response.HttpStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/game")
public class GameController {

    private final GameService gameService;

    @GetMapping("/{gameId}")
    public ApiDataResponse gameDetailInfo(@PathVariable int gameId) {
        GameDetailResponseDto responseDto = gameService.getGameDetailInfo(gameId);
        return ApiDataResponse.of(HttpStatusCode.OK, responseDto, "성공적으로 게임 세부 내용을 조회했습니다.");
    }


}
