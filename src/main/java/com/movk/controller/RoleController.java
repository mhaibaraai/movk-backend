/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.role.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.RoleService;
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
 * 角色管理 Controller
 */
@Tag(name = "角色管理", description = "角色相关接口")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色分页列表
     */
    @Operation(summary = "获取角色分页列表", description = "根据查询条件分页查询角色信息")
    @GetMapping
    @RequiresPermission("system:role:list")
    public R<Page<RoleResp>> getRolePage(
            RoleQueryReq query,
            @PageableDefault(sort = "roleSort", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return R.success(roleService.getRolePage(query, pageable));
    }

    /**
     * 获取角色详情
     */
    @Operation(summary = "获取角色详情", description = "根据角色 ID 查询角色详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:role:query")
    public R<RoleDetailResp> getRoleById(@PathVariable UUID id) {
        return R.success(roleService.getRoleDetail(id));
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色", description = "创建新角色")
    @PostMapping
    @RequiresPermission("system:role:add")
    @Log(module = "角色管理", operation = CREATE)
    public R<UUID> createRole(@Valid @RequestBody RoleCreateReq req) {
        return R.success(roleService.createRole(req));
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色", description = "修改角色信息")
    @PutMapping
    @RequiresPermission("system:role:edit")
    @Log(module = "角色管理", operation = UPDATE)
    public R<Void> updateRole(@Valid @RequestBody RoleUpdateReq req) {
        roleService.updateRole(req);
        return R.ok();
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "根据角色 ID 删除角色")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:role:delete")
    @Log(module = "角色管理", operation = DELETE)
    public R<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    /**
     * 批量删除角色
     */
    @Operation(summary = "批量删除角色", description = "根据角色 ID 列表批量删除角色")
    @DeleteMapping("/batch")
    @RequiresPermission("system:role:delete")
    @Log(module = "角色管理", operation = DELETE)
    public R<Void> deleteRoles(@RequestBody @NotEmpty(message = "角色ID列表不能为空") List<UUID> ids) {
        roleService.deleteRoles(ids);
        return R.ok();
    }

    /**
     * 分配角色菜单权限
     */
    @Operation(summary = "分配菜单权限", description = "为角色分配菜单权限")
    @PostMapping("/{roleId}/menus")
    @RequiresPermission("system:role:edit")
    @Log(module = "角色管理", operation = UPDATE)
    public R<Void> assignMenus(@PathVariable UUID roleId, @RequestBody @NotEmpty(message = "菜单ID列表不能为空") List<UUID> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return R.ok();
    }

    /**
     * 检查角色编码是否存在
     */
    @Operation(summary = "检查角色编码", description = "检查角色编码是否已存在")
    @GetMapping("/exists")
    @RequiresPermission("system:role:query")
    public R<Boolean> checkRoleCode(@RequestParam String code) {
        return R.success(roleService.existsByCode(code));
    }
}
