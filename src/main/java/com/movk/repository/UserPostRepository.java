/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.entity.UserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 用户岗位关联 Repository
 */
@Repository
public interface UserPostRepository extends JpaRepository<UserPost, Long> {

    /**
     * 根据用户ID查询岗位ID列表
     */
    @Query("SELECT up.postId FROM UserPost up WHERE up.userId = :userId")
    List<UUID> findPostIdsByUserId(@Param("userId") UUID userId);

    /**
     * 根据岗位ID查询用户ID列表
     */
    @Query("SELECT up.userId FROM UserPost up WHERE up.postId = :postId")
    List<UUID> findUserIdsByPostId(@Param("postId") UUID postId);

    /**
     * 根据用户ID删除所有关联
     */
    @Modifying
    @Query("DELETE FROM UserPost up WHERE up.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * 根据岗位ID删除所有关联
     */
    @Modifying
    @Query("DELETE FROM UserPost up WHERE up.postId = :postId")
    void deleteByPostId(@Param("postId") UUID postId);

    /**
     * 检查用户岗位关联是否存在
     */
    @Query("SELECT COUNT(up) > 0 FROM UserPost up " +
           "WHERE up.userId = :userId AND up.postId = :postId")
    boolean existsByUserIdAndPostId(
        @Param("userId") UUID userId,
        @Param("postId") UUID postId
    );
}
