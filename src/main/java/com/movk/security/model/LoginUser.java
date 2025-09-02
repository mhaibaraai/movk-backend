/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.security.model;

import com.movk.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Spring Security 登录用户信息
 * 实现 UserDetails 接口，用于 Spring Security 认证和授权
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    /**
     * 用户ID
     */
    private UUID id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 角色代码列表
     */
    private List<String> roles;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 获取用户权限集合
     * Spring Security 要求的方法，将角色转换为 GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * 获取密码
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 获取用户名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期
     * 根据用户状态判断账户是否可用
     */
    @Override
    public boolean isAccountNonExpired() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * 账户是否未锁定
     * 检查用户状态是否为锁定状态
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    /**
     * 凭证是否未过期
     * 在此实现中，凭证不会过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     * 只有激活状态的用户才被认为是启用的
     */
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
