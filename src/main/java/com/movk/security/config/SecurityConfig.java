/*
 * @Author yixuanmiao
 * @Date 2025/08/31 01:54
 */

package com.movk.security.config;

import com.movk.security.filter.JwtAuthenticationFilter;
import com.movk.security.handler.RestAccessDeniedHandler;
import com.movk.security.handler.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 * 配置认证、授权、过滤器链等安全相关设置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         RestAuthenticationEntryPoint authenticationEntryPoint,
                         RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * 密码编码器配置
     * 使用 BCrypt 编码器进行密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 安全过滤器链配置
     * 配置 HTTP 安全、认证规则、异常处理等
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF 保护（因为使用 JWT 无状态认证）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用 CORS（如果需要，可以在 CorsConfig 中单独配置）
            .cors(AbstractHttpConfigurer::disable)
            
            // 禁用默认的 Session 管理，使用无状态的 JWT 认证
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            
            // 配置请求授权规则
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(SecurityEndpoints.PUBLIC_APIS).permitAll()
                .anyRequest().authenticated())
            
            // 添加 JWT 认证过滤器，在用户名密码认证过滤器之前执行
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
