/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.security.model.LoginUser;

import java.util.UUID;

/**
 * 会话管理服务接口
 */
public interface SessionService {

    /**
     * 创建会话（登录成功后调用）
     *
     * @param token       JWT token
     * @param loginUser   登录用户信息
     * @param expireSeconds token 有效期（秒）
     */
    void createSession(String token, LoginUser loginUser, long expireSeconds);

    /**
     * 销毁会话（登出时调用）
     */
    void destroySession(String token);

    /**
     * 刷新会话过期时间
     */
    void refreshSession(String token, long expireSeconds);

    /**
     * 检查会话是否有效
     */
    boolean isSessionValid(String token);

    /**
     * 踢出用户所有会话（单点登录互踢或强制下线）
     */
    void kickOutUser(UUID userId);

    /**
     * 踢出用户其他会话，保留当前会话（新登录时调用，实现单点登录）
     *
     * @param userId       用户 ID
     * @param currentToken 当前登录的 token（保留）
     */
    void kickOutOtherSessions(UUID userId, String currentToken);

    /**
     * 获取用户当前会话数
     */
    int getUserSessionCount(UUID userId);

    /**
     * 检查是否允许登录（根据最大会话数限制）
     *
     * @param userId      用户 ID
     * @param maxSessions 最大允许会话数，-1 表示不限制
     * @return true 允许登录，false 不允许
     */
    boolean allowLogin(UUID userId, int maxSessions);
}
