/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.menu.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
import com.movk.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 菜单管理 Controller
 */
@Tag(name = "菜单管理", description = "菜单相关接口")
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
@Validated
public class MenuController {

    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    /**
     * 获取菜单树（管理端 - 完整树）
     */
    @Operation(summary = "获取菜单树", description = "获取完整的菜单树形结构")
    @GetMapping("/tree")
    @RequiresPermission("system:menu:list")
    public R<List<MenuResp>> getMenuTree() {
        return R.success(menuService.getMenuTree());
    }

    /**
     * 获取菜单列表（扁平）
     */
    @Operation(summary = "获取菜单列表", description = "获取扁平化的菜单列表")
    @GetMapping
    @RequiresPermission("system:menu:list")
    public R<List<MenuResp>> getMenuList() {
        return R.success(menuService.getAllMenus());
    }

    /**
     * 获取菜单详情
     */
    @Operation(summary = "获取菜单详情", description = "根据菜单 ID 查询菜单详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:menu:query")
    public R<MenuResp> getMenuById(@PathVariable UUID id) {
        return R.success(menuService.getMenuById(id));
    }

    /**
     * 新增菜单
     */
    @Operation(summary = "新增菜单", description = "创建新菜单")
    @PostMapping
    @RequiresPermission("system:menu:add")
    @Log(module = "菜单管理", operation = CREATE)
    public R<UUID> createMenu(@Valid @RequestBody MenuCreateReq req) {
        return R.success(menuService.createMenu(req));
    }

    /**
     * 修改菜单
     */
    @Operation(summary = "修改菜单", description = "修改菜单信息")
    @PutMapping
    @RequiresPermission("system:menu:edit")
    @Log(module = "菜单管理", operation = UPDATE)
    public R<Void> updateMenu(@Valid @RequestBody MenuUpdateReq req) {
        menuService.updateMenu(req);
        return R.ok();
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单", description = "根据菜单 ID 删除菜单")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:menu:delete")
    @Log(module = "菜单管理", operation = DELETE)
    public R<Void> deleteMenu(@PathVariable UUID id) {
        menuService.deleteMenu(id);
        return R.ok();
    }

    /**
     * 获取当前用户的路由菜单树（前端路由用）
     */
    @Operation(summary = "获取用户路由", description = "获取当前用户的路由菜单树")
    @GetMapping("/user/routes")
    @PreAuthorize("isAuthenticated()")
    public R<List<MenuTreeResp>> getUserRoutes() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserMenuTree(loginUser.getId()));
    }

    /**
     * 获取当前用户的权限标识列表
     */
    @Operation(summary = "获取用户权限标识", description = "获取当前用户的所有权限标识列表")
    @GetMapping("/user/permissions")
    @PreAuthorize("isAuthenticated()")
    public R<List<String>> getUserPermissions() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserPermissions(loginUser.getId()));
    }

    /**
     * 获取当前用户的按钮权限集合
     */
    @Operation(summary = "获取用户按钮权限", description = "获取当前用户的所有按钮权限集合")
    @GetMapping("/user/buttons")
    @PreAuthorize("isAuthenticated()")
    public R<Set<String>> getUserButtonPermissions() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserButtonPermissions(loginUser.getId()));
    }

    /**
     * 获取当前用户的按钮权限（按菜单分组）
     */
    @Operation(summary = "获取按菜单分组的按钮权限", description = "获取当前用户的按钮权限并按菜单分组")
    @GetMapping("/user/buttons-by-menu")
    @PreAuthorize("isAuthenticated()")
    public R<Map<UUID, Set<String>>> getUserButtonPermissionsByMenu() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserButtonPermissionsByMenu(loginUser.getId()));
    }

    /**
     * 获取所有权限标识列表（用于角色分配权限时选择）
     */
    @Operation(summary = "获取所有权限标识", description = "获取系统所有权限标识列表")
    @GetMapping("/permissions/all")
    @RequiresPermission("system:menu:list")
    public R<List<String>> getAllPermissionCodes() {
        return R.success(menuService.getAllPermissionCodes());
    }
}
