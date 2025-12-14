/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.user.*;
import com.movk.security.annotation.DataPermission;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 用户管理 Controller
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 获取用户分页列表
     */
    @Operation(summary = "获取用户分页列表", description = "根据查询条件分页查询用户信息")
    @GetMapping
    @RequiresPermission("system:user:list")
    @DataPermission(deptIdColumn = "dept_id")
    public R<Page<UserResp>> getUserPage(
            UserQueryReq query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return R.success(userService.getUserPage(query, pageable));
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据用户 ID 查询用户详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:user:query")
    public R<UserDetailResp> getUserById(@PathVariable UUID id) {
        return R.success(userService.getUserDetail(id));
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户", description = "创建新用户，并分配角色和岗位")
    @PostMapping
    @RequiresPermission("system:user:add")
    @Log(module = "用户管理", operation = CREATE, excludeParamNames = {"password"})
    public R<UUID> createUser(@Valid @RequestBody UserCreateReq req) {
        return R.success(userService.createUser(req));
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户", description = "修改用户信息，可同时更新角色和岗位")
    @PutMapping
    @RequiresPermission("system:user:edit")
    @Log(module = "用户管理", operation = UPDATE)
    public R<Void> updateUser(@Valid @RequestBody UserUpdateReq req) {
        userService.updateUser(req);
        return R.ok();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据用户 ID 删除用户")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:user:delete")
    @Log(module = "用户管理", operation = DELETE)
    public R<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return R.ok();
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "根据用户 ID 列表批量删除用户")
    @DeleteMapping("/batch")
    @RequiresPermission("system:user:delete")
    @Log(module = "用户管理", operation = DELETE)
    public R<Void> deleteUsers(@RequestBody @NotEmpty(message = "用户ID列表不能为空") List<UUID> ids) {
        userService.deleteUsers(ids);
        return R.ok();
    }

    /**
     * 分配用户角色
     */
    @Operation(summary = "分配角色", description = "为用户分配角色")
    @PostMapping("/{userId}/roles")
    @RequiresPermission("system:user:edit")
    @Log(module = "用户管理", operation = UPDATE)
    public R<Void> assignRoles(@PathVariable UUID userId, @RequestBody @NotEmpty(message = "角色ID列表不能为空") List<UUID> roleIds) {
        userService.assignRoles(userId, roleIds);
        return R.ok();
    }

    /**
     * 分配用户岗位
     */
    @Operation(summary = "分配岗位", description = "为用户分配岗位")
    @PostMapping("/{userId}/posts")
    @RequiresPermission("system:user:edit")
    @Log(module = "用户管理", operation = UPDATE)
    public R<Void> assignPosts(@PathVariable UUID userId, @RequestBody @NotEmpty(message = "岗位ID列表不能为空") List<UUID> postIds) {
        userService.assignPosts(userId, postIds);
        return R.ok();
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    @PutMapping("/reset-password")
    @RequiresPermission("system:user:resetPwd")
    @Log(module = "用户管理", operation = UPDATE, excludeParamNames = {"newPassword"})
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        userService.resetPassword(req);
        return R.ok();
    }

    /**
     * 检查用户名是否存在
     * 需要登录状态，防止用户枚举攻击
     */
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    @GetMapping("/check-username/{username}")
    @RequiresPermission("system:user:query")
    public R<Boolean> checkUsername(@PathVariable String username) {
        return R.success(userService.existsByUsername(username));
    }

    /**
     * 检查邮箱是否存在
     * 需要登录状态，防止用户枚举攻击
     */
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    @GetMapping("/check-email/{email}")
    @RequiresPermission("system:user:query")
    public R<Boolean> checkEmail(@PathVariable String email) {
        return R.success(userService.existsByEmail(email));
    }
}
