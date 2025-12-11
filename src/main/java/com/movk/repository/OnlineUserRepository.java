/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.entity.OnlineUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 在线用户 Repository
 */
@Repository
public interface OnlineUserRepository extends JpaRepository<OnlineUser, UUID>, JpaSpecificationExecutor<OnlineUser> {

    /**
     * 根据会话ID查询在线用户
     */
    Optional<OnlineUser> findBySessionId(String sessionId);

    /**
     * 根据用户ID查询所有在线会话
     */
    List<OnlineUser> findByUserIdOrderByLoginTimeDesc(UUID userId);

    /**
     * 根据会话ID删除在线用户
     */
    @Modifying
    @Query("DELETE FROM OnlineUser ou WHERE ou.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID删除所有在线会话
     */
    @Modifying
    @Query("DELETE FROM OnlineUser ou WHERE ou.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * 清除过期的在线用户
     */
    @Modifying
    @Query("DELETE FROM OnlineUser ou WHERE ou.expireTime < :now")
    void deleteByExpireTimeBefore(@Param("now") OffsetDateTime now);

    /**
     * 查询所有在线用户（未过期）
     */
    @Query("SELECT ou FROM OnlineUser ou " +
           "WHERE ou.expireTime > :now " +
           "ORDER BY ou.loginTime DESC")
    List<OnlineUser> findAllActive(@Param("now") OffsetDateTime now);

    /**
     * 统计在线用户数
     */
    @Query("SELECT COUNT(ou) FROM OnlineUser ou WHERE ou.expireTime > :now")
    long countActive(@Param("now") OffsetDateTime now);
}
