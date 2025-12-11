/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.security.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movk.common.enums.DataScope;
import com.movk.entity.Role;
import com.movk.entity.User;
import com.movk.repository.RoleRepository;
import com.movk.repository.UserRepository;
import com.movk.security.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        return buildLoginUser(user, roles, roleCodes);
    }

    public LoginUser buildLoginUser(String username, List<String> roles) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        List<Role> roleEntities = roleRepository.findRolesByUserId(user.getId());
        return buildLoginUser(user, roleEntities, roles);
    }

    private LoginUser buildLoginUser(User user, List<Role> roleEntities, List<String> roleCodes) {
        // 计算数据权限范围（取所有角色中最大的权限范围）
        DataScope dataScope = calculateDataScope(roleEntities);
        Set<UUID> dataScopeDeptIds = collectDataScopeDeptIds(roleEntities, dataScope);

        return LoginUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .roles(roleCodes)
                .status(user.getStatus())
                .deptId(user.getDeptId())
                .dataScope(dataScope)
                .dataScopeDeptIds(dataScopeDeptIds)
                .build();
    }

    /**
     * 计算数据权限范围
     * 取所有角色中权限范围最大的（数值最小的）
     * 权限范围优先级：ALL(1) > CUSTOM(5) > DEPT_AND_CHILD(3) > DEPT(2) > SELF(4)
     */
    private DataScope calculateDataScope(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return DataScope.SELF;
        }

        // 按照权限范围的优先级排序
        return roles.stream()
                .map(Role::getDataScope)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(this::getDataScopePriority))
                .orElse(DataScope.SELF);
    }

    /**
     * 获取数据权限范围的优先级（数值越小优先级越高）
     */
    private int getDataScopePriority(DataScope dataScope) {
        return switch (dataScope) {
            case ALL -> 1;
            case CUSTOM -> 2;
            case DEPT_AND_CHILD -> 3;
            case DEPT -> 4;
            case SELF -> 5;
        };
    }

    /**
     * 收集自定义数据权限部门ID
     * 当数据范围为 CUSTOM 时，合并所有角色的自定义部门
     */
    private Set<UUID> collectDataScopeDeptIds(List<Role> roles, DataScope dataScope) {
        if (dataScope != DataScope.CUSTOM || roles == null) {
            return null;
        }

        Set<UUID> deptIds = new HashSet<>();
        for (Role role : roles) {
            if (role.getDataScope() == DataScope.CUSTOM && role.getDataScopeDeptIds() != null) {
                try {
                    List<String> ids = objectMapper.readValue(
                        role.getDataScopeDeptIds(),
                        new TypeReference<List<String>>() {}
                    );
                    ids.stream()
                        .map(UUID::fromString)
                        .forEach(deptIds::add);
                } catch (Exception e) {
                    log.warn("解析角色 {} 的自定义部门ID失败: {}", role.getCode(), e.getMessage());
                }
            }
        }
        return deptIds.isEmpty() ? null : deptIds;
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
