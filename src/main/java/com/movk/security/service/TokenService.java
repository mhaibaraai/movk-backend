/*
 * @Author yixuanmiao
 * @Date 2025/09/02 13:18
 */

package com.movk.security.service;

import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtService jwtService;

    public TokenService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthTokensDTO generateTokens(LoginUser loginUser) {
        String accessToken = jwtService.generateToken(loginUser);
        long expirationMillis = jwtService.getExpirationDateFromToken(accessToken).getTime();
        long nowMillis = System.currentTimeMillis();
        long remainingSeconds = Math.max(0L, (expirationMillis - nowMillis) / 1000L);

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

        String username = jwtService.getUsernameFromToken(oldToken);
        java.util.UUID userId = jwtService.getUserIdFromToken(oldToken);
        String displayName = jwtService.getDisplayNameFromToken(oldToken);
        java.util.List<String> roles = jwtService.getRolesFromToken(oldToken);

        LoginUser loginUser = LoginUser.builder()
                .id(userId)
                .username(username)
                .displayName(displayName)
                .roles(roles)
                .build();

        return generateTokens(loginUser);
    }

    public void revokeToken(String token) {
        // JWT 是无状态的，无法真正撤销令牌
        // 可以考虑将令牌加入黑名单，或缩短令牌有效期
        // 这里留空以示意
    }
}
