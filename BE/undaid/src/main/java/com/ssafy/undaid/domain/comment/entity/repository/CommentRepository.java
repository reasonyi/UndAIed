package com.ssafy.undaid.domain.comment.entity.repository;

import com.ssafy.undaid.domain.comment.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Integer> {
    Page<Comments> findByBoard_BoardId(Integer boardId, Pageable pageable);
}