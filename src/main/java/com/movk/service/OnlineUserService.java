/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.entity.OnlineUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * 在线用户服务接口
 */
public interface OnlineUserService {

    /**
     * 记录用户上线
     *
     * @param sessionId   会话 ID（token）
     * @param userId      用户 ID
     * @param username    用户名
     * @param deptId      部门 ID
     * @param deptName    部门名称
     * @param expireSeconds Token 有效期（秒）
     */
    void online(String sessionId, UUID userId, String username, UUID deptId, String deptName, long expireSeconds);

    /**
     * 记录用户下线
     */
    void offline(String sessionId);

    /**
     * 通过用户ID强制下线
     */
    void forceOfflineByUserId(UUID userId);

    /**
     * 通过会话ID强制下线（单个会话）
     */
    void forceOfflineBySessionId(String sessionId);

    /**
     * 分页查询在线用户
     */
    Page<OnlineUser> listOnlineUsers(String username, String loginIp, Pageable pageable);

    /**
     * 查询所有在线用户
     */
    List<OnlineUser> listAllOnlineUsers();

    /**
     * 查询用户的所有在线会话
     */
    List<OnlineUser> listUserSessions(UUID userId);

    /**
     * 根据会话ID查询在线用户
     */
    OnlineUser getBySessionId(String sessionId);

    /**
     * 统计在线用户数
     */
    long countOnlineUsers();

    /**
     * 清理过期的在线用户记录
     */
    void cleanExpiredUsers();

    /**
     * 刷新在线用户的过期时间
     */
    void refreshExpireTime(String sessionId, long expireSeconds);

    /**
     * 检查会话是否有效（未过期）
     */
    boolean isSessionValid(String sessionId);

    /**
     * 将 Token 加入黑名单
     */
    void addToBlacklist(String token, long expireSeconds);

    /**
     * 检查 Token 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);
}
