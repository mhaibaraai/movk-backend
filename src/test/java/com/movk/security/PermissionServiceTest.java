/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security;

import com.movk.common.enums.DataScope;
import com.movk.common.enums.EnableStatus;
import com.movk.repository.MenuRepository;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
import com.movk.security.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 权限服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService 单元测试")
class PermissionServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private PermissionService permissionService;

    private LoginUser adminUser;
    private LoginUser normalUser;

    @BeforeEach
    void setUp() {
        // 超级管理员用户
        adminUser = LoginUser.builder()
            .id(UUID.randomUUID())
            .username("admin")
            .roles(List.of("admin"))
            .deptId(UUID.randomUUID())
            .dataScope(DataScope.ALL)
            .build();

        // 普通用户
        normalUser = LoginUser.builder()
            .id(UUID.randomUUID())
            .username("user")
            .roles(List.of("user"))
            .deptId(UUID.randomUUID())
            .dataScope(DataScope.DEPT)
            .build();
    }

    @Nested
    @DisplayName("hasPermission 方法测试")
    class HasPermissionTests {

        @Test
        @DisplayName("超级管理员应该拥有所有权限")
        void superAdminShouldHaveAllPermissions() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(adminUser);

            // When
            boolean result = permissionService.hasPermission("system:user:list");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户拥有分配的权限")
        void normalUserShouldHaveAssignedPermissions() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:user:list", "system:user:query"));

            // When
            boolean result = permissionService.hasPermission("system:user:list");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户没有未分配的权限")
        void normalUserShouldNotHaveUnassignedPermissions() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:user:list"));

            // When
            boolean result = permissionService.hasPermission("system:user:delete");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("空角色列表应该返回 false")
        void emptyRolesShouldReturnFalse() {
            // Given
            List<String> emptyRoles = List.of();

            // When
            boolean result = permissionService.hasPermission(emptyRoles, "system:user:list");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("空权限码应该返回 false")
        void nullPermissionShouldReturnFalse() {
            // Given
            List<String> roles = List.of("user");

            // When
            boolean result = permissionService.hasPermission(roles, null);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyPermission 方法测试")
    class HasAnyPermissionTests {

        @Test
        @DisplayName("超级管理员应该拥有任意权限")
        void superAdminShouldHaveAnyPermission() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(adminUser);

            // When
            boolean result = permissionService.hasAnyPermission(
                "system:user:list", "system:role:list"
            );

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户拥有任一权限时返回 true")
        void normalUserWithAnyPermissionShouldReturnTrue() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:user:list"));

            // When
            boolean result = permissionService.hasAnyPermission(
                "system:user:list", "system:role:list"
            );

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户没有任一权限时返回 false")
        void normalUserWithoutAnyPermissionShouldReturnFalse() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:dept:list"));

            // When
            boolean result = permissionService.hasAnyPermission(
                "system:user:list", "system:role:list"
            );

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAllPermissions 方法测试")
    class HasAllPermissionsTests {

        @Test
        @DisplayName("超级管理员应该拥有所有权限")
        void superAdminShouldHaveAllPermissions() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(adminUser);

            // When
            boolean result = permissionService.hasAllPermissions(
                "system:user:list", "system:role:list"
            );

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户拥有全部权限时返回 true")
        void normalUserWithAllPermissionsShouldReturnTrue() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:user:list", "system:role:list"));

            // When
            boolean result = permissionService.hasAllPermissions(
                "system:user:list", "system:role:list"
            );

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户缺少部分权限时返回 false")
        void normalUserMissingPermissionShouldReturnFalse() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:user:list"));

            // When
            boolean result = permissionService.hasAllPermissions(
                "system:user:list", "system:role:list"
            );

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuperAdmin 方法测试")
    class IsSuperAdminTests {

        @Test
        @DisplayName("admin 角色应该是超级管理员")
        void adminRoleShouldBeSuperAdmin() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(adminUser);

            // When
            boolean result = permissionService.isSuperAdmin();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("普通用户不是超级管理员")
        void normalUserShouldNotBeSuperAdmin() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);

            // When
            boolean result = permissionService.isSuperAdmin();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getCurrentUserPermissions 方法测试")
    class GetCurrentUserPermissionsTests {

        @Test
        @DisplayName("应该返回当前用户的所有权限")
        void shouldReturnCurrentUserPermissions() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);
            when(menuRepository.findPermissionCodesByRoleCodesAndStatus(
                anyList(), any(EnableStatus.class)
            )).thenReturn(List.of("system:user:list", "system:user:query"));

            // When
            Set<String> permissions = permissionService.getCurrentUserPermissions();

            // Then
            assertThat(permissions).containsExactlyInAnyOrder(
                "system:user:list", "system:user:query"
            );
        }

        @Test
        @DisplayName("超级管理员应该返回全部权限标识")
        void superAdminShouldReturnAllPermissions() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(adminUser);

            // When
            Set<String> permissions = permissionService.getCurrentUserPermissions();

            // Then
            assertThat(permissions).contains("*:*:*");
        }
    }
}
