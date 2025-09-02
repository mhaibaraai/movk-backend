/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.repository;

import com.movk.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    /**
     * 根据角色ID和权限代码检查权限是否存在
     * 
     * @param roleIds 角色ID列表
     * @param permissionCode 权限代码
     * @return 是否存在
     */
    @Query("select count(rp) > 0 from RolePermission rp where rp.role.id in :roleIds and rp.permission.code = :permissionCode")
    boolean existsByRoleIdsAndCode(@Param("roleIds") Collection<UUID> roleIds, @Param("permissionCode") String permissionCode);

    /**
     * 根据角色ID列表获取权限代码
     * 
     * @param roleIds 角色ID列表
     * @return 权限代码列表
     */
    @Query("select distinct rp.permission.code from RolePermission rp where rp.role.id in :roleIds")
    List<String> findPermissionCodesByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
