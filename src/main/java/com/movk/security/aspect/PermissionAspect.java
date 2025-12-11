/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.aspect;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.security.annotation.RequiresPermission;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
import com.movk.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 权限校验切面
 * 拦截标注了 @RequiresPermission 注解的方法，进行权限校验
 */
@Slf4j
@Aspect
@Component
@Order(1) // 优先级高于数据权限切面
@RequiredArgsConstructor
public class PermissionAspect {

    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;

    /**
     * 超级管理员角色编码
     */
    private static final String SUPER_ADMIN_ROLE = "admin";

    /**
     * 环绕通知：权限校验
     */
    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        // 获取当前登录用户
        LoginUser loginUser = currentUserService.getCurrentUser();
        List<String> userRoles = loginUser.getRoles();

        // 超级管理员跳过权限校验
        if (isSuperAdmin(userRoles)) {
            return joinPoint.proceed();
        }

        // 获取所需权限
        String[] permissions = requiresPermission.value();
        RequiresPermission.Logical logical = requiresPermission.logical();

        // 校验权限
        boolean hasPermission = checkPermissions(userRoles, permissions, logical);

        if (!hasPermission) {
            log.warn("权限校验失败 - 用户: {}, 需要权限: {}, 逻辑: {}",
                loginUser.getUsername(), Arrays.toString(permissions), logical);
            throw new BusinessException(RCode.FORBIDDEN, "没有操作权限，请联系管理员");
        }

        // 记录权限校验日志
        if (log.isDebugEnabled()) {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            log.debug("权限校验通过 - 用户: {}, 方法: {}.{}, 权限: {}",
                loginUser.getUsername(),
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                Arrays.toString(permissions));
        }

        return joinPoint.proceed();
    }

    /**
     * 类级别权限校验（方法未标注注解时，检查类上的注解）
     */
    @Around("@within(requiresPermission) && !@annotation(com.movk.security.annotation.RequiresPermission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        return checkPermission(joinPoint, requiresPermission);
    }

    /**
     * 检查是否为超级管理员
     */
    private boolean isSuperAdmin(List<String> roles) {
        return roles != null && roles.contains(SUPER_ADMIN_ROLE);
    }

    /**
     * 校验用户是否拥有指定权限
     *
     * @param userRoles   用户角色列表
     * @param permissions 所需权限列表
     * @param logical     逻辑关系
     * @return 是否有权限
     */
    private boolean checkPermissions(List<String> userRoles, String[] permissions, RequiresPermission.Logical logical) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        // 获取用户所有权限
        List<String> userPermissions = permissionService.getPermissionsByRoles(userRoles);

        if (logical == RequiresPermission.Logical.AND) {
            // AND 逻辑：必须拥有所有权限
            return Arrays.stream(permissions).allMatch(userPermissions::contains);
        } else {
            // OR 逻辑：拥有任意一个权限即可
            return Arrays.stream(permissions).anyMatch(userPermissions::contains);
        }
    }
}
