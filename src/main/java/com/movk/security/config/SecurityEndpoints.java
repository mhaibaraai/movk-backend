/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.security.config;

public final class SecurityEndpoints {

    private SecurityEndpoints() {}

    public static final String[] PUBLIC_APIS = new String[] {
            "/auth/login",
            "/auth/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/static/**",
            "/public/**"
    };
}
