/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.repository;

import com.movk.adapters.persistence.rbac.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
}
