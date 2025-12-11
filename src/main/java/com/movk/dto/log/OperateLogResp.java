/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.log;

import com.movk.common.enums.BusinessStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 操作日志响应
 */
@Data
public class OperateLogResp {

    private Long id;

    /**
     * 链路追踪 ID
     */
    private String traceId;

    /**
     * 操作人 ID
     */
    private UUID userId;

    /**
     * 操作人用户名
     */
    private String username;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 方法名
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求 URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求体
     */
    private String requestBody;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    private Integer operationTime;

    /**
     * 状态
     */
    private BusinessStatus status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private OffsetDateTime createdAt;
}
