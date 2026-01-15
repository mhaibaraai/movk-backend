/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.repository;

import com.movk.entity.UserRole;
import com.movk.entity.id.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * 根据用户ID删除所有角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.id.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * 根据用户ID查询角色ID列表
     */
    @Query("SELECT ur.id.roleId FROM UserRole ur WHERE ur.id.userId = :userId")
    List<UUID> findRoleIdsByUserId(@Param("userId") UUID userId);

    /**
     * 根据角色ID删除所有用户关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.id.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);

    /**
     * 检查角色是否被用户使用
     */
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRole ur WHERE ur.id.roleId = :roleId")
    boolean existsByRoleId(@Param("roleId") UUID roleId);
}
