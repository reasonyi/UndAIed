package com.ssafy.undaid.domain.user.entity.repository;

import com.ssafy.undaid.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    boolean existsByEmail(String email);
    Users findByEmail(String email);


    // =============== 친구 추가를 위해 추가할 내용
    Optional<Users> findByNickname(String nickname);
}
