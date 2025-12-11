/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.entity.OnlineUser;
import com.movk.security.model.LoginUser;
import com.movk.service.OnlineUserService;
import com.movk.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 会话管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final OnlineUserService onlineUserService;

    /**
     * 是否启用单点登录（同一用户只能有一个会话）
     */
    @Value("${security.session.single-login:false}")
    private boolean singleLogin;

    /**
     * 最大会话数（-1 表示不限制）
     */
    @Value("${security.session.max-sessions:-1}")
    private int maxSessions;

    @Override
    @Transactional
    public void createSession(String token, LoginUser loginUser, long expireSeconds) {
        // 单点登录模式：踢出其他会话
        if (singleLogin) {
            kickOutOtherSessions(loginUser.getId(), token);
        }

        // 记录在线用户
        onlineUserService.online(
            token,
            loginUser.getId(),
            loginUser.getUsername(),
            loginUser.getDeptId(),
            loginUser.getDeptName(),
            expireSeconds
        );

        log.debug("会话创建成功 - userId: {}, username: {}", loginUser.getId(), loginUser.getUsername());
    }

    @Override
    @Transactional
    public void destroySession(String token) {
        onlineUserService.offline(token);
        log.debug("会话销毁成功 - token: {}", token);
    }

    @Override
    @Transactional
    public void refreshSession(String token, long expireSeconds) {
        onlineUserService.refreshExpireTime(token, expireSeconds);
    }

    @Override
    public boolean isSessionValid(String token) {
        return onlineUserService.isSessionValid(token);
    }

    @Override
    @Transactional
    public void kickOutUser(UUID userId) {
        onlineUserService.forceOfflineByUserId(userId);
        log.info("用户所有会话已踢出 - userId: {}", userId);
    }

    @Override
    @Transactional
    public void kickOutOtherSessions(UUID userId, String currentToken) {
        List<OnlineUser> sessions = onlineUserService.listUserSessions(userId);

        for (OnlineUser session : sessions) {
            if (!session.getSessionId().equals(currentToken)) {
                onlineUserService.forceOfflineBySessionId(session.getSessionId());
                log.debug("踢出会话 - sessionId: {}", session.getSessionId());
            }
        }

        if (sessions.size() > 1) {
            log.info("单点登录互踢完成 - userId: {}, 踢出会话数: {}", userId, sessions.size() - 1);
        }
    }

    @Override
    public int getUserSessionCount(UUID userId) {
        return onlineUserService.listUserSessions(userId).size();
    }

    @Override
    public boolean allowLogin(UUID userId, int maxSessions) {
        if (maxSessions < 0) {
            // 不限制
            return true;
        }

        int currentSessions = getUserSessionCount(userId);
        return currentSessions < maxSessions;
    }
}
