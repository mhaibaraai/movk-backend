/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 角色菜单关联 Repository
 */
@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    @Query("SELECT rm.id.menuId FROM RoleMenu rm WHERE rm.id.roleId = :roleId")
    List<UUID> findMenuIdsByRoleId(@Param("roleId") UUID roleId);

    /**
     * 根据菜单ID查询角色ID列表
     */
    @Query("SELECT rm.id.roleId FROM RoleMenu rm WHERE rm.id.menuId = :menuId")
    List<UUID> findRoleIdsByMenuId(@Param("menuId") UUID menuId);

    /**
     * 根据角色ID删除所有关联
     */
    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.id.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);

    /**
     * 根据菜单ID删除所有关联
     */
    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.id.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") UUID menuId);

    /**
     * 检查角色菜单关联是否存在
     */
    @Query("SELECT COUNT(rm) > 0 FROM RoleMenu rm " +
           "WHERE rm.id.roleId = :roleId AND rm.id.menuId = :menuId")
    boolean existsByRoleIdAndMenuId(
        @Param("roleId") UUID roleId,
        @Param("menuId") UUID menuId
    );
}
