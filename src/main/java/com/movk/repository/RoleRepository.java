/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.repository;

import com.movk.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {

    @Query("select ur.role from UserRole ur where ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);

    /**
     * 根据角色代码列表批量查询角色
     *
     * @param roleCodes 角色代码列表
     * @return 角色列表
     */
    List<Role> findByCodeIn(Collection<String> roleCodes);

}
