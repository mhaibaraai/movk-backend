/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.log;

import com.movk.common.enums.BusinessStatus;
import com.movk.common.enums.LoginType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 登录日志响应
 */
@Data
public class LoginLogResp {

    private Long id;

    /**
     * 链路追踪 ID
     */
    private String traceId;

    /**
     * 用户 ID
     */
    private UUID userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 登录 IP
     */
    private String loginIp;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 状态
     */
    private BusinessStatus status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 登录时间
     */
    private OffsetDateTime createdAt;
}
