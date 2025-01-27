package com.ssafy.undaid.domain.game.entity.respository;

import com.ssafy.undaid.domain.game.entity.GameParticipants;
import com.ssafy.undaid.domain.game.entity.Games;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameParticipantsRepository extends JpaRepository<GameParticipants, Integer> {
    List<GameParticipants> findDistinctByUserUserId(int userId);
}
