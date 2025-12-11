/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.common.enums.BusinessStatus;
import com.movk.entity.OperateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 操作日志服务接口
 */
public interface OperateLogService {

    /**
     * 异步保存操作日志
     */
    void saveLogAsync(OperateLog log);

    /**
     * 分页查询操作日志
     *
     * @param userId    用户 ID（可选）
     * @param module    模块（可选）
     * @param operation 操作类型（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageable  分页参数
     * @return 分页结果
     */
    Page<OperateLog> listOperateLogs(UUID userId, String module, String operation,
                                     BusinessStatus status, OffsetDateTime startTime,
                                     OffsetDateTime endTime, Pageable pageable);

    /**
     * 根据 ID 查询操作日志详情
     */
    OperateLog getById(Long id);

    /**
     * 根据用户 ID 查询操作日志
     */
    List<OperateLog> listByUserId(UUID userId);

    /**
     * 清理指定天数之前的日志
     *
     * @param days 天数
     * @return 清理的记录数
     */
    int cleanLogs(int days);

    /**
     * 导出操作日志
     *
     * @param userId    用户 ID（可选）
     * @param module    模块（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 日志列表
     */
    List<OperateLog> exportLogs(UUID userId, String module, BusinessStatus status,
                                OffsetDateTime startTime, OffsetDateTime endTime);
}
