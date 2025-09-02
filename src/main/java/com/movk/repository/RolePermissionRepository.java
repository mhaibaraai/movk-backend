/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.repository;

import com.movk.entity.RolePermission;
import com.movk.entity.id.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

}
