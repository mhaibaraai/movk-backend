/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.aspect;

import com.movk.security.annotation.DataPermission;
import com.movk.security.datascope.DataPermissionContext;
import com.movk.security.datascope.DataPermissionContextHolder;
import com.movk.security.datascope.DataPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据权限切面
 * 拦截标注了 @DataPermission 注解的方法，设置数据权限上下文
 */
@Slf4j
@Aspect
@Component
@Order(2) // 优先级低于权限校验切面
@RequiredArgsConstructor
public class DataPermissionAspect {

    private final DataPermissionService dataPermissionService;

    /**
     * 环绕通知：数据权限处理
     */
    @Around("@annotation(dataPermission)")
    public Object handleDataPermission(ProceedingJoinPoint joinPoint, DataPermission dataPermission) throws Throwable {
        try {
            // 构建数据权限上下文
            DataPermissionContext context = dataPermissionService.buildContext(
                dataPermission.deptAlias(),
                dataPermission.userAlias(),
                dataPermission.deptIdColumn(),
                dataPermission.userIdColumn()
            );

            // 设置上下文
            DataPermissionContextHolder.setContext(context);

            if (log.isDebugEnabled()) {
                Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                log.debug("数据权限上下文设置 - 方法: {}.{}, 数据范围: {}, 部门IDs: {}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    context.getDataScope(),
                    context.getDataScopeDeptIds());
            }

            // 执行目标方法
            return joinPoint.proceed();
        } finally {
            // 清除上下文，防止内存泄漏
            DataPermissionContextHolder.clearContext();
        }
    }
}
