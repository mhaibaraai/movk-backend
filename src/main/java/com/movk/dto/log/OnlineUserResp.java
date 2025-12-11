/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.log;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 在线用户响应
 */
@Data
public class OnlineUserResp {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private UUID userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 部门 ID
     */
    private UUID deptId;

    /**
     * 部门名称
     */
    private String deptName;

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
     * 登录时间
     */
    private OffsetDateTime loginTime;

    /**
     * 过期时间
     */
    private OffsetDateTime expireTime;
}
