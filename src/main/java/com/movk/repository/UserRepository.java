/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.repository;

import com.movk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查询用户（登录时使用，仅查询未删除且启用的用户）
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据用户名查询用户（含角色信息，减少 N+1 查询）
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH UserRole ur ON ur.id.userId = u.id " +
           "LEFT JOIN FETCH Role r ON ur.id.roleId = r.id AND r.deleted = false " +
           "WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * 检查用户名是否存在（排除指定用户）
     */
    boolean existsByUsernameAndIdNot(String username, UUID id);

    /**
     * 检查邮箱是否存在（排除指定用户）
     */
    boolean existsByEmailAndIdNot(String email, UUID id);

    /**
     * 检查手机号是否存在（排除指定用户）
     */
    boolean existsByPhoneAndIdNot(String phone, UUID id);
}
