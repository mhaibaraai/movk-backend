/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.config;

import com.movk.security.service.PermissionService;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * 自定义方法安全表达式根对象
 * 扩展 Spring Security 安全表达式，添加自定义权限校验方法
 *
 * <p>支持在 @PreAuthorize 中使用以下表达式：
 * <ul>
 *   <li>hasPermission('system:user:list') - 检查是否有指定权限</li>
 *   <li>hasAnyPermission('system:user:list', 'system:user:query') - 检查是否有任一权限</li>
 *   <li>hasAllPermissions('system:user:list', 'system:user:query') - 检查是否拥有所有权限</li>
 *   <li>isSuperAdmin() - 检查是否为超级管理员</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * &#64;PreAuthorize("hasPermission('system:user:list')")
 * public List&lt;User&gt; getUserList() { ... }
 *
 * &#64;PreAuthorize("hasAnyPermission('system:user:list', 'system:user:query')")
 * public User getUser(UUID id) { ... }
 *
 * &#64;PreAuthorize("isSuperAdmin()")
 * public void dangerousOperation() { ... }
 * </pre>
 */
public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private final PermissionService permissionService;
    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication authentication,
                                              PermissionService permissionService) {
        super(authentication);
        this.permissionService = permissionService;
    }

    /**
     * 检查是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        return permissionService.hasPermission(permission);
    }

    /**
     * 检查是否拥有任意一个权限
     *
     * @param permissions 权限标识数组
     * @return 是否有任一权限
     */
    public boolean hasAnyPermission(String... permissions) {
        return permissionService.hasAnyPermission(permissions);
    }

    /**
     * 检查是否拥有所有权限
     *
     * @param permissions 权限标识数组
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        return permissionService.hasAllPermissions(permissions);
    }

    /**
     * 检查是否为超级管理员
     *
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return permissionService.isSuperAdmin();
    }

    // ==================== MethodSecurityExpressionOperations 实现 ====================

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return target;
    }

    public void setThis(Object target) {
        this.target = target;
    }
}
