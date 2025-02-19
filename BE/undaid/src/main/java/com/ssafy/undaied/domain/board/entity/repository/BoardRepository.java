package com.ssafy.undaied.domain.board.entity.repository;


import com.ssafy.undaied.domain.board.entity.Boards;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Boards, Integer> {
    Page<Boards> findByCategory(Byte category, Pageable pageable);
}