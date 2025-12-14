/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.dto.log;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 操作日志查询参数
 */
@Data
public class OperateLogQuery {

    /**
     * 用户ID
     */
    private UUID userId;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 状态：1-成功 2-失败
     */
    private Short status;

    /**
     * 开始时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endTime;
}
