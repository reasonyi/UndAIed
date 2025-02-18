package com.ssafy.undaied.socket.vote.dto.response;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@NoArgsConstructor
public class VoteResultResponseDto {
    private final Integer number = 0;
    private String content;

    @Builder
    public VoteResultResponseDto(String content) {
        this.content = content;
    }

    public static VoteResultResponseDto notifyVoteResult(String eliminatedNumber, int receivedCount, boolean isAI, boolean isInfected) {
        String type;
        if (!isAI) {
            if (!isInfected) {
                type = "인간";
            }
            else {
                type = "감염된 인간";
            }
        } else {
            type = "AI";
        }

        return VoteResultResponseDto.builder()
                .content(type + " 익명" + eliminatedNumber + " 플레이어가 총 " + receivedCount + "표를 받아 처형되었습니다.")
                .build();
    }

    public static VoteResultResponseDto notifyDraw(List<Integer> maxVotedCandidates, int receivedCount) {
        List<String> numbers = maxVotedCandidates.stream().map(number -> "익명" + number.toString()).collect(Collectors.toList());
        String numbersString = String.join(", ", numbers);
        String content = numbersString + " 플레이어가 각각 " + receivedCount + "표를 받아 처형 대상자가 없습니다.";

        return VoteResultResponseDto.builder()
                .content(content)
                .build();
    }
}
