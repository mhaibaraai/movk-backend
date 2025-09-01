/*
 * @Author yixuanmiao
 * @Date 2025/09/01 22:54
 */

package com.movk.security.config;

public final class SecurityEndpoints {

    private SecurityEndpoints() {}

    public static final String[] PUBLIC_APIS = new String[] {
            "/auth/login",
            "/auth/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/movk-backend/swagger-ui/**",
            "/api/movk-backend/v3/api-docs/**",
            "/actuator/health",
            "/static/**",
            "/public/**"
    };
}
