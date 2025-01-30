package com.ssafy.undaid.domain.game.entity.respository;

import com.ssafy.undaid.domain.game.entity.Games;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamesRepository extends JpaRepository<Games, Integer> {
    public Optional<Games> findById(int id);
}
