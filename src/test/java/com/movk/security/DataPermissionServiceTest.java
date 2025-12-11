/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security;

import com.movk.common.enums.DataScope;
import com.movk.repository.DepartmentRepository;
import com.movk.security.datascope.DataPermissionContext;
import com.movk.security.datascope.DataPermissionService;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
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
import static org.mockito.Mockito.when;

/**
 * 数据权限服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataPermissionService 单元测试")
class DataPermissionServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DataPermissionService dataPermissionService;

    private UUID userId;
    private UUID deptId;
    private UUID childDeptId;
    private LoginUser adminUser;
    private LoginUser normalUser;
    private LoginUser deptAndChildUser;
    private LoginUser customUser;
    private LoginUser selfUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        deptId = UUID.randomUUID();
        childDeptId = UUID.randomUUID();

        // 超级管理员
        adminUser = LoginUser.builder()
            .id(userId)
            .username("admin")
            .roles(List.of("admin"))
            .deptId(deptId)
            .dataScope(DataScope.ALL)
            .build();

        // 全部数据权限用户（非管理员）
        normalUser = LoginUser.builder()
            .id(userId)
            .username("manager")
            .roles(List.of("manager"))
            .deptId(deptId)
            .dataScope(DataScope.ALL)
            .build();

        // 本部门及子部门权限用户
        deptAndChildUser = LoginUser.builder()
            .id(userId)
            .username("deptleader")
            .roles(List.of("leader"))
            .deptId(deptId)
            .dataScope(DataScope.DEPT_AND_CHILD)
            .build();

        // 自定义部门权限用户
        customUser = LoginUser.builder()
            .id(userId)
            .username("custom")
            .roles(List.of("user"))
            .deptId(deptId)
            .dataScope(DataScope.CUSTOM)
            .dataScopeDeptIds(Set.of(deptId, childDeptId))
            .build();

        // 仅本人数据权限用户
        selfUser = LoginUser.builder()
            .id(userId)
            .username("self")
            .roles(List.of("user"))
            .deptId(deptId)
            .dataScope(DataScope.SELF)
            .build();
    }

    @Nested
    @DisplayName("getAccessibleDeptIds(LoginUser) 方法测试")
    class GetAccessibleDeptIdsTests {

        @Test
        @DisplayName("超级管理员 - 返回 null 表示无限制")
        void superAdminShouldReturnNull() {
            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds(adminUser);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("DataScope.ALL - 返回 null 表示无限制")
        void dataScopeAllShouldReturnNull() {
            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds(normalUser);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("DataScope.DEPT - 返回仅本部门")
        void dataScopeDeptShouldReturnOwnDept() {
            // Given
            LoginUser deptUser = LoginUser.builder()
                .id(userId)
                .username("deptuser")
                .roles(List.of("user"))
                .deptId(deptId)
                .dataScope(DataScope.DEPT)
                .build();

            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds(deptUser);

            // Then
            assertThat(result).containsExactly(deptId);
        }

        @Test
        @DisplayName("DataScope.DEPT_AND_CHILD - 返回本部门及子部门")
        void dataScopeDeptAndChildShouldReturnDeptWithChildren() {
            // Given
            when(departmentRepository.findDeptAndChildIds(deptId))
                .thenReturn(List.of(deptId, childDeptId));

            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds(deptAndChildUser);

            // Then
            assertThat(result).containsExactlyInAnyOrder(deptId, childDeptId);
        }

        @Test
        @DisplayName("DataScope.CUSTOM - 返回自定义部门列表")
        void dataScopeCustomShouldReturnCustomDepts() {
            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds(customUser);

            // Then
            assertThat(result).containsExactlyInAnyOrder(deptId, childDeptId);
        }

        @Test
        @DisplayName("DataScope.SELF - 返回空集合")
        void dataScopeSelfShouldReturnEmptySet() {
            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds(selfUser);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAccessibleDeptIds() 无参方法测试")
    class GetAccessibleDeptIdsNoArgsTests {

        @Test
        @DisplayName("应该从当前用户获取数据权限")
        void shouldGetFromCurrentUser() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(customUser);

            // When
            Set<UUID> result = dataPermissionService.getAccessibleDeptIds();

            // Then
            assertThat(result).containsExactlyInAnyOrder(deptId, childDeptId);
        }
    }

    @Nested
    @DisplayName("isSelfDataScope 方法测试")
    class IsSelfDataScopeTests {

        @Test
        @DisplayName("SELF 范围应返回 true")
        void selfScopeShouldReturnTrue() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(selfUser);

            // When
            boolean result = dataPermissionService.isSelfDataScope();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("超级管理员应返回 false")
        void superAdminShouldReturnFalse() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(adminUser);

            // When
            boolean result = dataPermissionService.isSelfDataScope();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("其他范围应返回 false")
        void otherScopesShouldReturnFalse() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(normalUser);

            // When
            boolean result = dataPermissionService.isSelfDataScope();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getCurrentUserId 方法测试")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("应该返回当前用户 ID")
        void shouldReturnCurrentUserId() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(selfUser);

            // When
            UUID result = dataPermissionService.getCurrentUserId();

            // Then
            assertThat(result).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("buildContext 方法测试")
    class BuildContextTests {

        @Test
        @DisplayName("应该构建正确的数据权限上下文")
        void shouldBuildCorrectContext() {
            // Given
            when(currentUserService.getCurrentUser()).thenReturn(customUser);

            // When
            DataPermissionContext context = dataPermissionService.buildContext(
                "d", "u", "dept_id", "creator"
            );

            // Then
            assertThat(context.getUserId()).isEqualTo(userId);
            assertThat(context.getDeptId()).isEqualTo(deptId);
            assertThat(context.getDataScope()).isEqualTo(DataScope.CUSTOM);
            assertThat(context.getDataScopeDeptIds()).containsExactlyInAnyOrder(deptId, childDeptId);
            assertThat(context.getDeptAlias()).isEqualTo("d");
            assertThat(context.getUserAlias()).isEqualTo("u");
            assertThat(context.getDeptIdColumn()).isEqualTo("dept_id");
            assertThat(context.getUserIdColumn()).isEqualTo("creator");
        }
    }
}
