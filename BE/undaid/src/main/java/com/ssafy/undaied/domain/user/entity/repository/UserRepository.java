package com.ssafy.undaied.domain.user.entity.repository;

import com.ssafy.undaied.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<Users> findByEmail(String email);
    Optional<Users> findById(int id);


    // =============== 친구 추가를 위해 추가할 내용
    Optional<Users> findByNickname(String nickname);
}
