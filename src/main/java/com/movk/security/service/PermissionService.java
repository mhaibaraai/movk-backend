/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.security.service;

import com.movk.entity.Role;
import com.movk.repository.PermissionRepository;
import com.movk.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("permissionService")
public class PermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PermissionService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean hasPermission(List<String> userRoles, String permission) {
        if (userRoles == null || userRoles.isEmpty() || permission == null || permission.trim().isEmpty()) {
            return false;
        }

        try {
            List<UUID> roleIds = getRoleIdsByRoleCodes(userRoles);
            return permissionRepository.existsByRoleIdsAndCode(roleIds, permission);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getPermissionsByRoles(List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }

        try {
            List<UUID> roleIds = getRoleIdsByRoleCodes(userRoles);
            return permissionRepository.findPermissionCodesByRoleIds(roleIds);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<UUID> getRoleIdsByRoleCodes(List<String> roleCodes) {
        List<Role> roles = roleRepository.findByCodeIn(roleCodes);
        return roles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());
    }
}
