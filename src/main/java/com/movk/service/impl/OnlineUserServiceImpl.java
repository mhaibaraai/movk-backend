/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.entity.OnlineUser;
import com.movk.repository.OnlineUserRepository;
import com.movk.service.OnlineUserService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final OnlineUserRepository onlineUserRepository;

    /**
     * Token 黑名单（内存存储，生产环境应使用 Redis）
     * Key: token, Value: 过期时间戳
     */
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void online(String sessionId, UUID userId, String username, UUID deptId, String deptName, long expireSeconds) {
        // 先删除已存在的会话（如果存在）
        onlineUserRepository.findBySessionId(sessionId).ifPresent(
            existing -> onlineUserRepository.deleteBySessionId(sessionId)
        );

        OnlineUser onlineUser = new OnlineUser();
        onlineUser.setSessionId(sessionId);
        onlineUser.setUserId(userId);
        onlineUser.setUsername(username);
        onlineUser.setDeptId(deptId);
        onlineUser.setDeptName(deptName);
        onlineUser.setLoginTime(OffsetDateTime.now());
        onlineUser.setExpireTime(OffsetDateTime.now().plusSeconds(expireSeconds));

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            onlineUser.setLoginIp(getClientIp(request));

            // 解析 User-Agent
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            onlineUser.setBrowser(userAgent.getBrowser().getName());
            onlineUser.setOs(userAgent.getOperatingSystem().getName());
        }

        onlineUserRepository.save(onlineUser);
        log.debug("用户上线 - userId: {}, username: {}, sessionId: {}", userId, username, sessionId);
    }

    @Override
    @Transactional
    public void offline(String sessionId) {
        onlineUserRepository.deleteBySessionId(sessionId);
        log.debug("用户下线 - sessionId: {}", sessionId);
    }

    @Override
    @Transactional
    public void forceOfflineByUserId(UUID userId) {
        // 获取用户所有会话
        List<OnlineUser> sessions = onlineUserRepository.findByUserIdOrderByLoginTimeDesc(userId);

        // 将所有会话的 token 加入黑名单
        for (OnlineUser session : sessions) {
            // 计算剩余过期时间
            long remainingSeconds = session.getExpireTime().toEpochSecond() - OffsetDateTime.now().toEpochSecond();
            if (remainingSeconds > 0) {
                addToBlacklist(session.getSessionId(), remainingSeconds);
            }
        }

        // 删除在线用户记录
        onlineUserRepository.deleteByUserId(userId);
        log.info("强制用户下线 - userId: {}, 会话数: {}", userId, sessions.size());
    }

    @Override
    @Transactional
    public void forceOfflineBySessionId(String sessionId) {
        onlineUserRepository.findBySessionId(sessionId).ifPresent(session -> {
            // 计算剩余过期时间
            long remainingSeconds = session.getExpireTime().toEpochSecond() - OffsetDateTime.now().toEpochSecond();
            if (remainingSeconds > 0) {
                addToBlacklist(sessionId, remainingSeconds);
            }
        });

        onlineUserRepository.deleteBySessionId(sessionId);
        log.info("强制会话下线 - sessionId: {}", sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OnlineUser> listOnlineUsers(String username, String loginIp, Pageable pageable) {
        OffsetDateTime now = OffsetDateTime.now();

        Specification<OnlineUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只查询未过期的
            predicates.add(cb.greaterThan(root.get("expireTime"), now));

            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (StringUtils.hasText(loginIp)) {
                predicates.add(cb.like(root.get("loginIp"), "%" + loginIp + "%"));
            }

            query.orderBy(cb.desc(root.get("loginTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return onlineUserRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineUser> listAllOnlineUsers() {
        return onlineUserRepository.findAllActive(OffsetDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineUser> listUserSessions(UUID userId) {
        return onlineUserRepository.findByUserIdOrderByLoginTimeDesc(userId).stream()
            .filter(session -> session.getExpireTime().isAfter(OffsetDateTime.now()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OnlineUser getBySessionId(String sessionId) {
        return onlineUserRepository.findBySessionId(sessionId)
            .filter(session -> session.getExpireTime().isAfter(OffsetDateTime.now()))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOnlineUsers() {
        return onlineUserRepository.countActive(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public void cleanExpiredUsers() {
        OffsetDateTime now = OffsetDateTime.now();
        onlineUserRepository.deleteByExpireTimeBefore(now);

        // 同时清理过期的黑名单
        long currentTime = System.currentTimeMillis();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() < currentTime);

        log.debug("清理过期在线用户记录完成");
    }

    @Override
    @Transactional
    public void refreshExpireTime(String sessionId, long expireSeconds) {
        onlineUserRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setExpireTime(OffsetDateTime.now().plusSeconds(expireSeconds));
            onlineUserRepository.save(session);
            log.debug("刷新会话过期时间 - sessionId: {}", sessionId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionId) {
        // 检查黑名单
        if (isTokenBlacklisted(sessionId)) {
            return false;
        }

        // 检查是否存在且未过期
        return onlineUserRepository.findBySessionId(sessionId)
            .map(session -> session.getExpireTime().isAfter(OffsetDateTime.now()))
            .orElse(false);
    }

    @Override
    public void addToBlacklist(String token, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + (expireSeconds * 1000);
        tokenBlacklist.put(token, expireTime);
        log.debug("Token 加入黑名单 - token: {}, expireAt: {}", token, expireTime);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        Long expireTime = tokenBlacklist.get(token);
        if (expireTime == null) {
            return false;
        }

        // 检查是否已过期
        if (expireTime < System.currentTimeMillis()) {
            tokenBlacklist.remove(token);
            return false;
        }

        return true;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
