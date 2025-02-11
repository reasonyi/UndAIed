package com.ssafy.undaied.socket.vote.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketExceptionHandler;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteHandler {

    private final SocketIOServer server;
    private final VoteService voteService;
    private final SocketExceptionHandler socketExceptionHandler;


    // 투표 제출
    public void submitVote(SocketIOClient client, VoteSubmitRequestDto voteSubmitRequestDto) {
        Integer userId = client.get("userId");
        Integer gameId = 1; // 테스트를 위해 임의로 설정
//        Integer gameId = client.get("gameId");
//        VoteSubmitResponseDto response = voteService.submitVote(userId, gameId, voteSubmitRequestDto);
    }

    // 투표 결과 알림
    public void notifyVoteResult(Integer gameId) {
        VoteResultResponseDto responseDto = voteService.computeVoteResult(gameId);
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.SHOW_VOTE_RESULT.getValue(), responseDto);
    }

}
