package com.ssafy.undaid.domain.friend.entity.repository;

import com.ssafy.undaid.domain.friend.dto.response.FriendRequestListResponseDto;
import com.ssafy.undaid.domain.friend.dto.response.FriendResponseDto;
import com.ssafy.undaid.domain.friend.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friends, Integer> {

    // 친구 목록 조회
    // userId 또는 friendId가 사용자이면서 status = 'ACCEPTED'인 friends 목록
    @Query("SELECT new com.ssafy.undaid.domain.friend.dto.response.FriendResponseDto("+
            "f.status, " +
            "f.user.userId, " +
            "f.friendUser.userId)" +
            "FROM Friends f WHERE " +
            "(f.user.userId = :userId OR " +
            "f.friendUser.userId = :userId) " +
            "AND f.status = com.ssafy.undaid.domain.friend.entity.FriendshipStatus.ACCEPTED"
    )
    List<FriendResponseDto> findByUserId(@Param("userId") Integer userId);

    // 친구 요청 조회
    // friendId가 사용자이면서 status = 'PENDING'인 friends 목록
    @Query("SELECT new com.ssafy.undaid.domain.friend.dto.response.FriendRequestListResponseDto(" +
            "f.friendshipId, " +
            "f.user.userId, " +
            "f.user.nickname) " +
            "FROM Friends f WHERE " +
            "f.friendUser.userId = :userId AND " +
            "f.status = com.ssafy.undaid.domain.friend.entity.FriendshipStatus.PENDING"
    )
    List<FriendRequestListResponseDto> findPendingByUserId(@Param("userId") Integer userId);

    // 특정 친구 목록(한 개) 조회
    @Query("SELECT f FROM Friends f WHERE " +
            "(f.user.userId = :userId AND f.friendUser.userId = :friendId) OR " +
            "(f.user.userId = :friendId AND f.friendUser.userId = :userId)"
    )
    Friends findByUserIdAndFriendId(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
}
