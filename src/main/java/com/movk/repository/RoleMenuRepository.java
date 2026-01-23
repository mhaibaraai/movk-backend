package com.movk.repository;

import com.movk.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, RoleMenu.Id> {

    @Query("SELECT rm.menuId FROM RoleMenu rm WHERE rm.roleId = :roleId")
    List<UUID> findMenuIdsByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT rm.roleId FROM RoleMenu rm WHERE rm.menuId = :menuId")
    List<UUID> findRoleIdsByMenuId(@Param("menuId") UUID menuId);

    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);

    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") UUID menuId);

    @Query("SELECT COUNT(rm) > 0 FROM RoleMenu rm WHERE rm.roleId = :roleId AND rm.menuId = :menuId")
    boolean existsByRoleIdAndMenuId(@Param("roleId") UUID roleId, @Param("menuId") UUID menuId);
}
