/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证令牌数据传输对象
 * 用于返回登录成功后的令牌信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokensDTO {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌类型，通常为 "Bearer"
     */
    private String tokenType;

    /**
     * 令牌过期时间（秒）
     * 从当前时间开始计算的剩余有效期
     */
    private Long expiresIn;
}
