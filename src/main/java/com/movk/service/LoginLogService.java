/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.common.enums.BusinessStatus;
import com.movk.common.enums.LoginType;
import com.movk.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {

    /**
     * 记录登录成功日志
     */
    void recordLoginSuccess(String username, UUID userId, LoginType loginType, String message);

    /**
     * 记录登录失败日志
     */
    void recordLoginFailure(String username, LoginType loginType, String message);

    /**
     * 记录登出日志
     */
    void recordLogout(String username, UUID userId);

    /**
     * 分页查询登录日志
     */
    Page<LoginLog> listLoginLogs(String username, String loginIp, BusinessStatus status,
                                 OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable);

    /**
     * 根据 ID 查询登录日志详情
     */
    LoginLog getById(Long id);

    /**
     * 根据用户名查询最近登录记录
     */
    LoginLog getLatestLoginByUsername(String username);

    /**
     * 导出登录日志
     */
    List<LoginLog> exportLogs(String username, String loginIp, BusinessStatus status,
                              OffsetDateTime startTime, OffsetDateTime endTime);

    /**
     * 清理指定天数之前的日志
     */
    int cleanLogs(int days);
}
