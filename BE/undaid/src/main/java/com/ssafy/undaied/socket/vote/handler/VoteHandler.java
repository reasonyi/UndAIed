package com.ssafy.undaied.socket.vote.handler;

import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteSubmitResponseDto;
import com.ssafy.undaied.socket.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteHandler {

    private final SocketIOServer server;
    private final VoteService voteService;


    // 투표 제출
    public void submitVote(Integer number, Integer gameId, VoteSubmitRequestDto voteSubmitRequestDto) {
        VoteSubmitResponseDto responseDto = voteService.submitVote(number, gameId, voteSubmitRequestDto);
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.SUBMIT_VOTE.getValue(), responseDto);
    }

    // 투표 결과 알림
    public void notifyVoteResult(Integer gameId) {
        VoteResultResponseDto responseDto = voteService.computeVoteResult(gameId);
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.SHOW_VOTE_RESULT.getValue(), responseDto);
    }

}
