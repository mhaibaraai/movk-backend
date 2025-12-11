/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.security.service;

import com.movk.base.config.CacheConfig;
import com.movk.common.enums.EnableStatus;
import com.movk.repository.MenuRepository;
import com.movk.security.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务
 * 基于菜单的权限查询实现
 */
@Slf4j
@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final MenuRepository menuRepository;
    private final CurrentUserService currentUserService;

    /**
     * 超级管理员角色编码
     */
    private static final String SUPER_ADMIN_ROLE = "admin";

    /**
     * 全部权限标识
     */
    private static final String ALL_PERMISSION = "*:*:*";

    /**
     * 检查用户是否有指定权限
     *
     * @param userRoles  用户角色代码列表
     * @param permission 权限代码
     * @return 是否有权限
     */
    public boolean hasPermission(List<String> userRoles, String permission) {
        if (userRoles == null || userRoles.isEmpty() || permission == null) {
            return false;
        }

        // 超级管理员拥有所有权限
        if (isSuperAdmin(userRoles)) {
            return true;
        }

        List<String> permissions = getPermissionsByRoles(userRoles);
        return permissions.contains(ALL_PERMISSION) || permissions.contains(permission);
    }

    /**
     * 检查当前登录用户是否有指定权限
     * 可用于 SpEL 表达式：@permissionService.hasPermission('system:user:list')
     *
     * @param permission 权限代码
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            return hasPermission(loginUser.getRoles(), permission);
        } catch (Exception e) {
            log.warn("获取当前用户失败，权限校验返回 false: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前登录用户是否拥有任意一个权限
     *
     * @param permissions 权限代码数组
     * @return 是否有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }

        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            List<String> userRoles = loginUser.getRoles();

            // 超级管理员拥有所有权限
            if (isSuperAdmin(userRoles)) {
                return true;
            }

            List<String> userPermissions = getPermissionsByRoles(userRoles);
            if (userPermissions.contains(ALL_PERMISSION)) {
                return true;
            }

            for (String permission : permissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("权限校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前登录用户是否拥有所有权限
     *
     * @param permissions 权限代码数组
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            List<String> userRoles = loginUser.getRoles();

            // 超级管理员拥有所有权限
            if (isSuperAdmin(userRoles)) {
                return true;
            }

            List<String> userPermissions = getPermissionsByRoles(userRoles);
            if (userPermissions.contains(ALL_PERMISSION)) {
                return true;
            }

            for (String permission : permissions) {
                if (!userPermissions.contains(permission)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("权限校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前登录用户是否为超级管理员
     *
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            return isSuperAdmin(loginUser.getRoles());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查角色列表中是否包含超级管理员角色
     */
    private boolean isSuperAdmin(List<String> roles) {
        return roles != null && roles.contains(SUPER_ADMIN_ROLE);
    }

    /**
     * 获取用户角色对应的所有权限码
     *
     * @param userRoles 用户角色代码列表
     * @return 权限码列表
     */
    @Cacheable(value = CacheConfig.USER_PERMISSIONS, key = "#userRoles.hashCode()", unless = "#result.isEmpty()")
    public List<String> getPermissionsByRoles(List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 超级管理员返回所有权限标识
        if (isSuperAdmin(userRoles)) {
            return List.of(ALL_PERMISSION);
        }

        return menuRepository.findPermissionCodesByRoleCodesAndStatus(
            userRoles,
            EnableStatus.ENABLED
        );
    }

    /**
     * 获取当前登录用户的所有权限码
     *
     * @return 权限码集合
     */
    public Set<String> getCurrentUserPermissions() {
        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            List<String> permissions = getPermissionsByRoles(loginUser.getRoles());
            return new HashSet<>(permissions);
        } catch (Exception e) {
            log.warn("获取当前用户权限失败: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
}
