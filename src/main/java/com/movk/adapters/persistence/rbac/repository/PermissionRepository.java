/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.repository;

import com.movk.adapters.persistence.rbac.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    @Query("select distinct p.code from UserRole ur " +
            " join ur.role r " +
            " join RolePermission rp on rp.role = r " +
            " join rp.permission p " +
            " where ur.user.id = :userId")
    List<String> findAllPermissionCodesByUserId(@Param("userId") UUID userId);
}