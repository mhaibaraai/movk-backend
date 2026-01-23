package com.movk.repository;

import com.movk.entity.UserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserPostRepository extends JpaRepository<UserPost, UserPost.Id> {

    @Query("SELECT up.postId FROM UserPost up WHERE up.userId = :userId")
    List<UUID> findPostIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT up.userId FROM UserPost up WHERE up.postId = :postId")
    List<UUID> findUserIdsByPostId(@Param("postId") UUID postId);

    @Modifying
    @Query("DELETE FROM UserPost up WHERE up.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM UserPost up WHERE up.postId = :postId")
    void deleteByPostId(@Param("postId") UUID postId);

    @Query("SELECT COUNT(up) > 0 FROM UserPost up WHERE up.userId = :userId AND up.postId = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);
}
