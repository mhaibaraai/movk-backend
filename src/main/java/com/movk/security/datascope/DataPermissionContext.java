/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.datascope;

import com.movk.common.enums.DataScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * 数据权限上下文
 * 存储当前请求的数据权限过滤条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPermissionContext {

    /**
     * 当前用户ID
     */
    private UUID userId;

    /**
     * 当前用户部门ID
     */
    private UUID deptId;

    /**
     * 数据权限范围
     */
    private DataScope dataScope;

    /**
     * 自定义数据权限部门ID列表
     */
    private Set<UUID> dataScopeDeptIds;

    /**
     * 部门表别名
     */
    private String deptAlias;

    /**
     * 用户表别名
     */
    private String userAlias;

    /**
     * 部门ID字段名
     */
    private String deptIdColumn;

    /**
     * 用户ID字段名（创建人字段）
     */
    private String userIdColumn;
}
