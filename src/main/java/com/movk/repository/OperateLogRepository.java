/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.BusinessStatus;
import com.movk.entity.OperateLog;
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
 * 操作日志 Repository
 */
@Repository
public interface OperateLogRepository extends JpaRepository<OperateLog, Long>, JpaSpecificationExecutor<OperateLog> {

    /**
     * 根据用户ID查询操作日志
     */
    List<OperateLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * 根据模块查询操作日志
     */
    List<OperateLog> findByModuleOrderByCreatedAtDesc(String module);

    /**
     * 根据状态查询操作日志
     */
    List<OperateLog> findByStatusOrderByCreatedAtDesc(BusinessStatus status);

    /**
     * 根据时间范围查询操作日志
     */
    @Query("SELECT ol FROM OperateLog ol " +
           "WHERE ol.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY ol.createdAt DESC")
    List<OperateLog> findByCreatedAtBetween(
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime
    );

    /**
     * 清空操作日志（保留最近N天）
     */
    @Modifying
    @Query("DELETE FROM OperateLog ol WHERE ol.createdAt < :beforeDate")
    void deleteByCreatedAtBefore(@Param("beforeDate") OffsetDateTime beforeDate);
}
