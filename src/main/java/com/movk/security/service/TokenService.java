/*
 * @Author yixuanmiao
 * @Date 2025/09/02 13:18
 */

package com.movk.security.service;

import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import com.movk.service.SessionService;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtService jwtService;
    private final SessionService sessionService;

    public TokenService(JwtService jwtService, SessionService sessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    public AuthTokensDTO generateTokens(LoginUser loginUser) {
        String accessToken = jwtService.generateToken(loginUser);
        long expirationMillis = jwtService.getExpirationDateFromToken(accessToken).getTime();
        long nowMillis = System.currentTimeMillis();
        long remainingSeconds = Math.max(0L, (expirationMillis - nowMillis) / 1000L);

        // 创建会话记录（在线用户）
        sessionService.createSession(accessToken, loginUser, remainingSeconds);

        return AuthTokensDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(remainingSeconds)
                .build();
    }

    public AuthTokensDTO refreshToken(String oldToken) {
        if (!jwtService.validateToken(oldToken)) {
            throw new IllegalArgumentException("无效的令牌");
        }

        // 销毁旧会话
        sessionService.destroySession(oldToken);

        String username = jwtService.getUsernameFromToken(oldToken);
        java.util.UUID userId = jwtService.getUserIdFromToken(oldToken);
        String nickname = jwtService.getDisplayNameFromToken(oldToken);
        java.util.List<String> roles = jwtService.getRolesFromToken(oldToken);

        LoginUser loginUser = LoginUser.builder()
                .id(userId)
                .username(username)
                .nickname(nickname)
                .roles(roles)
                .build();

        return generateTokens(loginUser);
    }

    public void revokeToken(String token) {
        // 销毁会话，使 token 失效
        sessionService.destroySession(token);
    }
}
