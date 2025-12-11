/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.BusinessStatus;
import com.movk.common.enums.LoginType;
import com.movk.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 登录日志 Repository
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long>, JpaSpecificationExecutor<LoginLog> {

    /**
     * 根据用户ID查询登录日志
     */
    List<LoginLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * 根据用户名查询登录日志
     */
    List<LoginLog> findByUsernameOrderByCreatedAtDesc(String username);

    /**
     * 根据登录状态查询登录日志
     */
    List<LoginLog> findByStatusOrderByCreatedAtDesc(BusinessStatus status);

    /**
     * 根据登录类型查询登录日志
     */
    List<LoginLog> findByLoginTypeOrderByCreatedAtDesc(LoginType loginType);

    /**
     * 根据时间范围查询登录日志
     */
    @Query("SELECT ll FROM LoginLog ll " +
           "WHERE ll.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY ll.createdAt DESC")
    List<LoginLog> findByCreatedAtBetween(
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime
    );

    /**
     * 清空登录日志（保留最近N天）
     */
    @Modifying
    @Query("DELETE FROM LoginLog ll WHERE ll.createdAt < :beforeDate")
    void deleteByCreatedAtBefore(@Param("beforeDate") OffsetDateTime beforeDate);

    /**
     * 查询用户最近的登录记录
     */
    @Query("SELECT ll FROM LoginLog ll " +
           "WHERE ll.username = :username " +
           "AND ll.status = :status " +
           "ORDER BY ll.createdAt DESC " +
           "LIMIT 1")
    LoginLog findLatestByUsernameAndStatus(
        @Param("username") String username,
        @Param("status") BusinessStatus status
    );
}
