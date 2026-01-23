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

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户分页列表")
    @GetMapping
    @RequiresPermission("system:user:list")
    @DataPermission(deptIdColumn = "dept_id")
    public R<Page<UserResp>> getUserPage(
            UserQueryReq query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return R.success(userService.getUserPage(query, pageable));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @RequiresPermission("system:user:query")
    public R<UserDetailResp> getUserById(@PathVariable UUID id) {
        return R.success(userService.getUserDetail(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @RequiresPermission("system:user:create")
    @Log(module = "用户管理", operation = CREATE, excludeParamNames = {"password"})
    public R<UUID> createUser(@Valid @RequestBody UserCreateReq req) {
        return R.success(userService.createUser(req));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    @RequiresPermission("system:user:update")
    @Log(module = "用户管理", operation = UPDATE)
    public R<Void> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateReq req) {
        userService.updateUser(id, req);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:user:delete")
    @Log(module = "用户管理", operation = DELETE)
    public R<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping
    @RequiresPermission("system:user:delete")
    @Log(module = "用户管理", operation = DELETE)
    public R<Void> deleteUsers(@RequestBody @NotEmpty(message = "用户ID列表不能为空") List<UUID> ids) {
        userService.deleteUsers(ids);
        return R.ok();
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{userId}/roles")
    @RequiresPermission("system:user:update")
    @Log(module = "用户管理", operation = UPDATE)
    public R<Void> assignRoles(@PathVariable UUID userId, @RequestBody @NotEmpty(message = "角色ID列表不能为空") List<UUID> roleIds) {
        userService.assignRoles(userId, roleIds);
        return R.ok();
    }

    @Operation(summary = "分配岗位")
    @PostMapping("/{userId}/posts")
    @RequiresPermission("system:user:update")
    @Log(module = "用户管理", operation = UPDATE)
    public R<Void> assignPosts(@PathVariable UUID userId, @RequestBody @NotEmpty(message = "岗位ID列表不能为空") List<UUID> postIds) {
        userService.assignPosts(userId, postIds);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/reset-password")
    @RequiresPermission("system:user:reset-password")
    @Log(module = "用户管理", operation = UPDATE, excludeParamNames = {"newPassword"})
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        userService.resetPassword(req);
        return R.ok();
    }

}
