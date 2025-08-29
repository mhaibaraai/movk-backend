/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.repository;

import com.movk.adapters.persistence.rbac.entity.UserRole;
import com.movk.adapters.persistence.rbac.entity.id.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
