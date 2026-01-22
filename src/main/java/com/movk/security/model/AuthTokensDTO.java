package com.movk.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证令牌数据传输对象
 * 双 Token 架构：AccessToken (JWT 短期) + RefreshToken (UUID 长期)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokensDTO {

    /**
     * 访问令牌 (JWT)
     * 用于 API 请求认证，有效期短（15 分钟）
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌 (UUID)
     * 用于刷新 AccessToken，有效期长（7 天/30 天）
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * AccessToken 过期时间（秒）
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * RefreshToken 过期时间（秒）
     */
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;
}
