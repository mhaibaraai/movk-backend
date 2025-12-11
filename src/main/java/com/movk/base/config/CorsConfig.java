/*
 * @Author yixuanmiao
 * @Date 2025/09/02 13:16
 */

package com.movk.base.config;

import com.movk.base.filter.TraceIdFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 * 开发环境允许所有来源，生产环境通过 cors.allowed-origins 配置具体域名
 */
@Configuration
public class CorsConfig {

    private final JwtHeaderProperties jwtHeaderProperties;

    /**
     * 允许的跨域来源，多个用逗号分隔
     * 开发环境默认 *，生产环境应配置具体域名
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    public CorsConfig(JwtHeaderProperties jwtHeaderProperties) {
        this.jwtHeaderProperties = jwtHeaderProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);

        // 根据配置设置允许的来源
        if ("*".equals(allowedOrigins)) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // 暴露必要响应头，允许客户端访问
        configuration.setExposedHeaders(List.of(
                TraceIdFilter.TRACE_ID_HEADER,
                jwtHeaderProperties.getName(),
                "Content-Disposition"
        ));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
