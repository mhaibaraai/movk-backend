package com.movk.security.service;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.entity.RefreshToken;
import com.movk.repository.RefreshTokenRepository;
import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Token 服务
 * 统一管理 AccessToken (JWT) 和 RefreshToken (数据库)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginUserDetailsService userDetailsService;

    @Value("${jwt.refresh-token.expiration:604800}")
    private int refreshTokenExpiration;

    @Value("${jwt.refresh-token.remember-me-expiration:2592000}")
    private int rememberMeExpiration;

    @Value("${security.session.single-login:false}")
    private boolean singleLogin;

    @Value("${security.session.max-sessions:-1}")
    private int maxSessions;

    /**
     * 生成双 Token
     *
     * @param loginUser  登录用户
     * @param rememberMe 是否记住我（影响 RefreshToken 有效期）
     */
    @Transactional
    public AuthTokensDTO generateTokenPair(LoginUser loginUser, boolean rememberMe) {
        // 单点登录：撤销其他会话
        if (singleLogin) {
            revokeAllUserTokens(loginUser.getId(), "单点登录互踢");
        } else if (maxSessions > 0) {
            enforceMaxSessions(loginUser.getId());
        }

        // 生成 AccessToken (JWT)
        String accessToken = jwtService.generateAccessToken(loginUser);

        // 生成 RefreshToken (UUID)
        int expiration = rememberMe ? rememberMeExpiration : refreshTokenExpiration;
        RefreshToken refreshToken = createRefreshToken(loginUser, expiration);

        log.debug("Token 生成成功 - userId: {}, username: {}, rememberMe: {}",
                loginUser.getId(), loginUser.getUsername(), rememberMe);

        return AuthTokensDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn((long) jwtService.getAccessTokenExpiration())
                .refreshExpiresIn((long) expiration)
                .build();
    }

    /**
     * 刷新 AccessToken
     *
     * @param refreshTokenStr RefreshToken 字符串
     * @return 新的 Token 对（AccessToken 更新，RefreshToken 不变）
     */
    @Transactional
    public AuthTokensDTO refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository
                .findValidToken(refreshTokenStr, OffsetDateTime.now())
                .orElseThrow(() -> new BusinessException(RCode.TOKEN_INVALID, "RefreshToken 无效或已过期"));

        // 更新最后使用时间
        refreshToken.touch();
        refreshTokenRepository.save(refreshToken);

        // 重新加载用户信息并生成新的 AccessToken
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(refreshToken.getUsername());
        String newAccessToken = jwtService.generateAccessToken(loginUser);

        // 计算 RefreshToken 剩余有效期
        long refreshRemainingSeconds = refreshToken.getExpiresAt().toEpochSecond()
                - OffsetDateTime.now().toEpochSecond();

        log.debug("AccessToken 刷新成功 - userId: {}, username: {}",
                refreshToken.getUserId(), refreshToken.getUsername());

        return AuthTokensDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn((long) jwtService.getAccessTokenExpiration())
                .refreshExpiresIn(Math.max(0, refreshRemainingSeconds))
                .build();
    }

    /**
     * 撤销 RefreshToken（登出）
     */
    @Transactional
    public void revokeRefreshToken(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.revoke("用户登出");
            refreshTokenRepository.save(token);
            log.debug("RefreshToken 已撤销 - userId: {}", token.getUserId());
        });
    }

    /**
     * 撤销用户所有 Token（踢出用户）
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId, String reason) {
        int count = refreshTokenRepository.revokeAllByUserId(userId, OffsetDateTime.now(), reason);
        if (count > 0) {
            log.info("用户所有 Token 已撤销 - userId: {}, count: {}, reason: {}", userId, count, reason);
        }
    }

    /**
     * 获取用户的活跃会话列表
     */
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(UUID userId) {
        return refreshTokenRepository.findActiveByUserId(userId, OffsetDateTime.now());
    }

    /**
     * 获取用户的活跃会话数
     */
    @Transactional(readOnly = true)
    public int getUserActiveSessionCount(UUID userId) {
        return refreshTokenRepository.countActiveByUserId(userId, OffsetDateTime.now());
    }

    /**
     * 定时清理过期的 RefreshToken（每天凌晨 3 点）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        // 清理 7 天前过期的 Token
        OffsetDateTime before = OffsetDateTime.now().minusDays(7);
        int deleted = refreshTokenRepository.deleteExpiredBefore(before);
        if (deleted > 0) {
            log.info("清理过期 RefreshToken 完成 - count: {}", deleted);
        }
    }

    /**
     * 创建 RefreshToken
     */
    private RefreshToken createRefreshToken(LoginUser loginUser, int expirationSeconds) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString().replace("-", ""))
                .userId(loginUser.getId())
                .username(loginUser.getUsername())
                .deviceInfo(extractDeviceInfo())
                .clientIp(extractClientIp())
                .issuedAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusSeconds(expirationSeconds))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * 强制执行最大会话数限制
     */
    private void enforceMaxSessions(UUID userId) {
        List<RefreshToken> activeSessions = refreshTokenRepository.findActiveByUserId(userId, OffsetDateTime.now());

        if (activeSessions.size() >= maxSessions) {
            // 撤销最老的会话，保留 maxSessions - 1 个
            int toRevoke = activeSessions.size() - maxSessions + 1;
            for (int i = activeSessions.size() - 1; i >= activeSessions.size() - toRevoke && i >= 0; i--) {
                RefreshToken oldestSession = activeSessions.get(i);
                oldestSession.revoke("超出最大会话数限制");
                refreshTokenRepository.save(oldestSession);
            }
            log.info("超出最大会话数，撤销旧会话 - userId: {}, revoked: {}", userId, toRevoke);
        }
    }

    /**
     * 提取设备信息
     */
    private String extractDeviceInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            return userAgent.getBrowser().getName() + "/" + userAgent.getOperatingSystem().getName();
        }
        return "Unknown";
    }

    /**
     * 提取客户端 IP
     */
    private String extractClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return getClientIp(request);
        }
        return null;
    }

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
