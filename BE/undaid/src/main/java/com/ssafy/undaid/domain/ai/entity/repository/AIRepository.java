package com.ssafy.undaid.domain.ai.entity.repository;

import com.ssafy.undaid.domain.ai.entity.AIs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIRepository extends JpaRepository<AIs, Integer> {
}
