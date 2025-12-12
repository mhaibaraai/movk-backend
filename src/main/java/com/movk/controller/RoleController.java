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
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色分页列表
     */
    @GetMapping("/page")
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
    @GetMapping("/{id}")
    @RequiresPermission("system:role:query")
    public R<RoleDetailResp> getRoleById(@PathVariable UUID id) {
        return R.success(roleService.getRoleDetail(id));
    }

    /**
     * 新增角色
     */
    @PostMapping
    @RequiresPermission("system:role:add")
    @Log(module = "角色管理", operation = CREATE)
    public R<UUID> createRole(@Valid @RequestBody RoleCreateReq req) {
        return R.success(roleService.createRole(req));
    }

    /**
     * 修改角色
     */
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
    @PostMapping("/assign-menus")
    @RequiresPermission("system:role:edit")
    @Log(module = "角色管理", operation = UPDATE)
    public R<Void> assignMenus(@Valid @RequestBody AssignMenuReq req) {
        roleService.assignMenus(req);
        return R.ok();
    }

    /**
     * 检查角色编码是否存在
     */
    @GetMapping("/check-code/{code}")
    @RequiresPermission("system:role:query")
    public R<Boolean> checkRoleCode(@PathVariable String code) {
        return R.success(roleService.existsByCode(code));
    }
}
