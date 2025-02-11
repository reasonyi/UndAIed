package com.ssafy.undaied.socket.vote.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteSubmitResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class VoteService {

    @AllArgsConstructor
    public class Player {
        boolean isSurvive;
        boolean isInfected;
        boolean isAI;
    }

    private final Map<Integer, List<Player>> gameSessionMap = new ConcurrentHashMap<>();
    private final SocketIOServer server;
    private final Map<Integer, int[]> voteSubmitionMap = new ConcurrentHashMap<>();
    private final Map<Integer, int[]> voteResultMap = new ConcurrentHashMap<>();
    private final Map<Integer, int[]> voteMaxMap = new ConcurrentHashMap<>();
    private final Map<Integer, List<Integer>> randomVoteMap = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        List<Player> gamePlayerList = Arrays.asList(
                new Player(true, false, false),
                new Player(true, false, false),
                new Player(true, false, false),
                new Player(true, false, false),
                new Player(true, false, false),
                new Player(true, false, false),
                new Player(true, false, true),
                new Player(true, false, true)
        );

        gameSessionMap.put(1, new ArrayList<>(gamePlayerList));
    }

    // 투표 제출
    public VoteSubmitResponseDto submitVote(Integer number, Integer gameId, VoteSubmitRequestDto voteSubmitRequestDto) {
        if (!voteSubmitionMap.containsKey(gameId)) {
            voteSubmitionMap.put(gameId, new int[9]);
            voteResultMap.put(gameId, new int[9]);
            voteMaxMap.put(gameId, new int[2]);
        }
        if (gameSessionMap.get(gameId).get(number).isSurvive) {
            voteSubmitionMap.get(gameId)[number] = voteSubmitRequestDto.getTarget();
        } else if (gameSessionMap.get(gameId).get(number).isAI) {
            voteSubmitionMap.get(gameId)[number] = -2;
        } else {
            voteSubmitionMap.get(gameId)[number] = -1;
        }
        VoteSubmitResponseDto responseDto = VoteSubmitResponseDto.builder()
                .number(number)
                .build();

        return responseDto;
    }

    // 투표 산출
    public VoteResultResponseDto computeVoteResult(Integer gameId) {
        int[] voteState = voteSubmitionMap.get(gameId);
        int[] voteResult = voteResultMap.get(gameId);
        int[] voteMax = voteMaxMap.get(gameId);
        for (int i = 0; i < voteState.length; i++) {
            if ((voteState[i] > 0))
                voteResult[voteState[i]]++;
        }
        voteMaxMap.put(gameId, new int[2]); //[0]: maxIndex, [1]: max
        for (int i = 0; i < voteState.length - 2; i++) {
            if (voteResult[i] > voteMax[1]) {
                voteMax[1] = voteResult[i];
                voteMax[0] = i;
            }
        }

        randomVoteTargetAI(gameId);
        voteResult[voteMax[0]] += 2;

        VoteResultResponseDto responseDto = VoteResultResponseDto.builder()
                .number(voteMax[0])
                .voteReceived(voteResult[voteMax[0]])
                .build();

        voteSubmitionMap.remove(gameId);
        voteResultMap.remove(gameId);
        voteMaxMap.remove(gameId);

        return responseDto;
    }

    public Integer randomVoteTargetAI(Integer gameId) {
        int[] voteState = voteSubmitionMap.get(gameId);
        int[] voteResult = voteResultMap.get(gameId);
        int[] voteMax = voteMaxMap.get(gameId);
        List<Integer> randomVoteList = randomVoteMap.get(gameId);

        for (int i = 0; i < voteState.length - 2; i++) {
            if (voteResult[i] == voteMax[1] && voteMax[1] > 0) {
                randomVoteList.add(i);
            }
        }

        // List가 비어있지 않다면 랜덤으로 하나 선택
        if (!randomVoteList.isEmpty()) {
            int randomIndex = (int) (Math.random() * randomVoteList.size());
            return randomVoteList.get(randomIndex);
        }

        return voteMax[0];
    }
}


