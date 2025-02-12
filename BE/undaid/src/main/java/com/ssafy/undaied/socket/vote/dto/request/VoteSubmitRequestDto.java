package com.ssafy.undaied.socket.vote.dto.request;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VoteSubmitRequestDto {
    private Integer target;
}