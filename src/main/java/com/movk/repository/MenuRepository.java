/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.EnableStatus;
import com.movk.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 菜单 Repository
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    /**
     * 查询所有未删除的菜单，按排序号排序
     */
    List<Menu> findByDeletedFalseOrderByOrderNumAsc();

    /**
     * 根据父菜单ID查询子菜单
     */
    List<Menu> findByParentIdAndDeletedFalseOrderByOrderNumAsc(UUID parentId);

    /**
     * 根据状态和可见性查询菜单
     */
    List<Menu> findByStatusAndVisibleAndDeletedFalseOrderByOrderNumAsc(
        EnableStatus status, Boolean visible
    );

    /**
     * 根据权限码查询菜单
     */
    List<Menu> findByPermissionCodeAndDeletedFalse(String permissionCode);

    /**
     * 查询指定角色的所有菜单（通过角色菜单关联表）
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN RoleMenu rm ON rm.menuId = m.id " +
           "JOIN Role r ON rm.roleId = r.id " +
           "WHERE r.code IN :roleCodes " +
           "AND m.status = :status " +
           "AND m.deleted = false " +
           "AND r.deleted = false " +
           "ORDER BY m.orderNum ASC")
    List<Menu> findByRoleCodesAndStatus(
        @Param("roleCodes") List<String> roleCodes,
        @Param("status") EnableStatus status
    );

    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN RoleMenu rm ON rm.menuId = m.id " +
           "JOIN Role r ON rm.roleId = r.id " +
           "WHERE r.code IN :roleCodes " +
           "AND m.status = :status " +
           "AND m.visible = :visible " +
           "AND m.deleted = false " +
           "AND r.deleted = false " +
           "ORDER BY m.orderNum ASC")
    List<Menu> findByRoleCodesAndStatusAndVisible(
        @Param("roleCodes") List<String> roleCodes,
        @Param("status") EnableStatus status,
        @Param("visible") Boolean visible
    );

    @Query("SELECT DISTINCT m.permissionCode FROM Menu m " +
           "JOIN RoleMenu rm ON rm.menuId = m.id " +
           "JOIN Role r ON rm.roleId = r.id " +
           "WHERE r.code IN :roleCodes " +
           "AND m.status = :status " +
           "AND m.permissionCode IS NOT NULL " +
           "AND m.deleted = false " +
           "AND r.deleted = false")
    List<String> findPermissionCodesByRoleCodesAndStatus(
        @Param("roleCodes") List<String> roleCodes,
        @Param("status") EnableStatus status
    );
}
