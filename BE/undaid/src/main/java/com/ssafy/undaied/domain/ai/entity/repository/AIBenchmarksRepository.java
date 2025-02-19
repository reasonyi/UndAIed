package com.ssafy.undaied.domain.ai.entity.repository;

import com.ssafy.undaied.domain.ai.entity.AIBenchmarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIBenchmarksRepository extends JpaRepository<AIBenchmarks, Integer> {
    Optional<AIBenchmarks> findById(Integer id);
    List<AIBenchmarks> findByGame_GameIdOrderByDeadRoundAsc(Integer gameId);
    List<AIBenchmarks> findByAi_AiIdOrderByDeadRoundAsc(Integer aiId);
}
