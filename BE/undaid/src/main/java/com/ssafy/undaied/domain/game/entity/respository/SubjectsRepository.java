package com.ssafy.undaied.domain.game.entity.respository;

import com.ssafy.undaied.domain.game.entity.Games;
import com.ssafy.undaied.domain.game.entity.Subjects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectsRepository extends JpaRepository<Subjects, Integer> {
    public Optional<Subjects> findById(int id);
}
