/*
 * @Author yixuanmiao
 * @Date 2025/09/02 13:16
 */

package com.movk.base.config;

import com.movk.base.filter.TraceIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private final JwtHeaderProperties jwtHeaderProperties;

    public CorsConfig(JwtHeaderProperties jwtHeaderProperties) {
        this.jwtHeaderProperties = jwtHeaderProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(List.of("*")); // 允许所有来源
        configuration.setAllowedMethods(List.of("*")); // 允许所有请求方法
        configuration.setAllowedHeaders(List.of("*")); // 允许所有请求头
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
