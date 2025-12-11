/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.datascope;

import com.movk.common.enums.DataScope;
import com.movk.repository.DepartmentRepository;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据权限服务
 * 提供数据权限过滤相关的核心逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataPermissionService {

    private final CurrentUserService currentUserService;
    private final DepartmentRepository departmentRepository;

    /**
     * 超级管理员角色编码
     */
    private static final String SUPER_ADMIN_ROLE = "admin";

    /**
     * 获取当前用户可访问的部门ID集合
     * 根据数据权限范围计算
     *
     * @return 部门ID集合，null 表示不限制（全部数据）
     */
    public Set<UUID> getAccessibleDeptIds() {
        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            return getAccessibleDeptIds(loginUser);
        } catch (Exception e) {
            log.warn("获取用户数据权限失败: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 获取指定用户可访问的部门ID集合
     *
     * @param loginUser 登录用户
     * @return 部门ID集合，null 表示不限制（全部数据）
     */
    public Set<UUID> getAccessibleDeptIds(LoginUser loginUser) {
        // 超级管理员拥有全部数据权限
        if (isSuperAdmin(loginUser.getRoles())) {
            return null;
        }

        DataScope dataScope = loginUser.getDataScope();
        if (dataScope == null) {
            dataScope = DataScope.SELF;
        }

        switch (dataScope) {
            case ALL:
                // 全部数据权限，不限制
                return null;

            case DEPT:
                // 仅本部门数据
                if (loginUser.getDeptId() != null) {
                    return Set.of(loginUser.getDeptId());
                }
                return Collections.emptySet();

            case DEPT_AND_CHILD:
                // 本部门及子部门数据
                if (loginUser.getDeptId() != null) {
                    List<UUID> deptIds = departmentRepository.findDeptAndChildIds(loginUser.getDeptId());
                    return new HashSet<>(deptIds);
                }
                return Collections.emptySet();

            case CUSTOM:
                // 自定义部门数据权限
                Set<UUID> customDeptIds = loginUser.getDataScopeDeptIds();
                return customDeptIds != null ? customDeptIds : Collections.emptySet();

            case SELF:
            default:
                // 仅本人数据，返回空集合（需要在查询时使用 creator 字段过滤）
                return Collections.emptySet();
        }
    }

    /**
     * 检查当前用户是否为"仅本人数据"权限
     */
    public boolean isSelfDataScope() {
        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            return !isSuperAdmin(loginUser.getRoles()) &&
                   loginUser.getDataScope() == DataScope.SELF;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取当前用户ID（用于"仅本人数据"场景）
     */
    public UUID getCurrentUserId() {
        try {
            return currentUserService.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否为超级管理员
     */
    private boolean isSuperAdmin(List<String> roles) {
        return roles != null && roles.contains(SUPER_ADMIN_ROLE);
    }

    /**
     * 构建数据权限上下文
     *
     * @param deptAlias    部门表别名
     * @param userAlias    用户表别名
     * @param deptIdColumn 部门ID字段名
     * @param userIdColumn 用户ID字段名
     * @return 数据权限上下文
     */
    public DataPermissionContext buildContext(String deptAlias, String userAlias,
                                               String deptIdColumn, String userIdColumn) {
        try {
            LoginUser loginUser = currentUserService.getCurrentUser();
            Set<UUID> accessibleDeptIds = getAccessibleDeptIds(loginUser);

            return DataPermissionContext.builder()
                .userId(loginUser.getId())
                .deptId(loginUser.getDeptId())
                .dataScope(loginUser.getDataScope())
                .dataScopeDeptIds(accessibleDeptIds)
                .deptAlias(deptAlias)
                .userAlias(userAlias)
                .deptIdColumn(deptIdColumn)
                .userIdColumn(userIdColumn)
                .build();
        } catch (Exception e) {
            log.warn("构建数据权限上下文失败: {}", e.getMessage());
            // 返回一个安全的默认上下文（仅本人数据）
            return DataPermissionContext.builder()
                .dataScope(DataScope.SELF)
                .dataScopeDeptIds(Collections.emptySet())
                .deptAlias(deptAlias)
                .userAlias(userAlias)
                .deptIdColumn(deptIdColumn)
                .userIdColumn(userIdColumn)
                .build();
        }
    }
}
