/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.config;

import com.movk.security.service.PermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * 自定义方法安全表达式处理器
 * 扩展 Spring Security 默认表达式处理器，添加自定义权限校验方法
 */
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final PermissionService permissionService;

    public CustomMethodSecurityExpressionHandler(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Authentication authentication, MethodInvocation invocation) {
        CustomMethodSecurityExpressionRoot root = 
            new CustomMethodSecurityExpressionRoot(authentication, permissionService);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(this.trustResolver);
        root.setRoleHierarchy(getRoleHierarchy());
        return root;
    }
}
