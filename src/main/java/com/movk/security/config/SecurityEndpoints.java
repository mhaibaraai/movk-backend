/*
 * @Author yixuanmiao
 * @Date 2025/09/02 13:19
 */

package com.movk.security.config;

public final class SecurityEndpoints {

    private SecurityEndpoints() {}

    public static final String[] PUBLIC_APIS = new String[] {
            // 认证相关
            "/auth/login",
            "/auth/register",
            // Swagger/OpenAPI 文档
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            // 健康检查
            "/actuator/health",
            // 静态资源
            "/static/**",
            "/public/**",
            "/favicon.ico"
    };
}
