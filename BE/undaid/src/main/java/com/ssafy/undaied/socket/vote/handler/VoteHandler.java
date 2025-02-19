package com.ssafy.undaied.socket.vote.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.exception.SocketExceptionHandler;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.response.AckResponse;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteSubmitResponseDto;
import com.ssafy.undaied.socket.vote.service.VoteService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoteHandler {

    private final SocketIONamespace namespace;
    private final VoteService voteService;

    @PostConstruct
    public void init() {
        namespace.addEventListener(EventType.SUBMIT_VOTE.getValue(), VoteSubmitRequestDto.class,
                (client, data, ack) -> {
                    try {
                        Integer userId = client.get("userId");
                        Integer gameId = client.get("gameId");

                        VoteSubmitResponseDto responseDto = voteService.submitVote(userId, gameId, data);
                        if (ack.isAckRequested()) {
                            ack.sendAckData(new AckResponse(true, null, responseDto));
                        }
                    } catch (SocketException e) {
                        log.error("SocketException in submitVote: {}", e.getMessage());
                        if (ack.isAckRequested()) {
                            ack.sendAckData(new AckResponse(false, e.getErrorCode().getMessage(), null));
                        }
                    } catch (Exception e) {
                        log.error("Unexpected error in submitVote: {}", e.getMessage());
                        if (ack.isAckRequested()) {
                            ack.sendAckData(new AckResponse(false, "Unexpected error occurred", null));
                        }
                    }
                });
    }
}
