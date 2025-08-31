/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.repository;

import com.movk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);

}
