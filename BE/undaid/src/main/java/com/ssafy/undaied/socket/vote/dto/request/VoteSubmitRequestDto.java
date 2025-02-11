package com.ssafy.undaied.socket.vote.dto.request;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoteSubmitRequestDto {
    private String target;

    public VoteSubmitRequestDto(String target) {
        this.target = target;
    }
}
