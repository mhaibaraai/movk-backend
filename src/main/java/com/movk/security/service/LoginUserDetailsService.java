/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.security.service;

import com.movk.entity.Role;
import com.movk.entity.User;
import com.movk.repository.RoleRepository;
import com.movk.repository.UserRepository;
import com.movk.security.model.LoginUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public LoginUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        return buildLoginUser(user, roleCodes);
    }

    public LoginUser buildLoginUser(String username, List<String> roles) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return buildLoginUser(user, roles);
    }

    private LoginUser buildLoginUser(User user, List<String> roles) {
        return LoginUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                .roles(roles)
                .status(user.getStatus())
                .build();
    }

    public boolean isUserValid(String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            return user != null && user.getStatus() == com.movk.common.enums.UserStatus.ACTIVE;
        } catch (Exception e) {
            return false;
        }
    }
}
