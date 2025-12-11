/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.config;

import com.movk.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

/**
 * 方法安全配置
 * 配置自定义的方法安全表达式处理器
 */
@Configuration
@RequiredArgsConstructor
public class MethodSecurityConfig {

    private final PermissionService permissionService;

    /**
     * 配置自定义方法安全表达式处理器
     * 使 @PreAuthorize 等注解支持自定义权限校验方法
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new CustomMethodSecurityExpressionHandler(permissionService);
    }
}
