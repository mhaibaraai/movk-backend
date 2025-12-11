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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 菜单管理 Controller
 */
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    /**
     * 获取菜单树（管理端 - 完整树）
     */
    @GetMapping("/tree")
    @RequiresPermission("system:menu:list")
    public R<List<MenuResp>> getMenuTree() {
        return R.success(menuService.getMenuTree());
    }

    /**
     * 获取菜单列表（扁平）
     */
    @GetMapping("/list")
    @RequiresPermission("system:menu:list")
    public R<List<MenuResp>> getMenuList() {
        return R.success(menuService.getAllMenus());
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:menu:query")
    public R<MenuResp> getMenuById(@PathVariable UUID id) {
        return R.success(menuService.getMenuById(id));
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @RequiresPermission("system:menu:add")
    @Log(module = "菜单管理", operation = CREATE)
    public R<UUID> createMenu(@RequestBody MenuCreateReq req) {
        return R.success(menuService.createMenu(req));
    }

    /**
     * 修改菜单
     */
    @PutMapping
    @RequiresPermission("system:menu:edit")
    @Log(module = "菜单管理", operation = UPDATE)
    public R<Void> updateMenu(@RequestBody MenuUpdateReq req) {
        menuService.updateMenu(req);
        return R.ok();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("system:menu:delete")
    @Log(module = "菜单管理", operation = DELETE)
    public R<Void> deleteMenu(@PathVariable UUID id) {
        menuService.deleteMenu(id);
        return R.ok();
    }

    /**
     * 获取当前用户的路由菜单树（前端路由用）
     * 不需要权限注解，登录用户即可访问
     */
    @GetMapping("/user/routes")
    public R<List<MenuTreeResp>> getUserRoutes() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserMenuTree(loginUser.getId()));
    }

    /**
     * 获取当前用户的权限标识列表
     * 不需要权限注解，登录用户即可访问
     */
    @GetMapping("/user/permissions")
    public R<List<String>> getUserPermissions() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserPermissions(loginUser.getId()));
    }

    /**
     * 获取当前用户的按钮权限集合
     * 不需要权限注解，登录用户即可访问
     */
    @GetMapping("/user/buttons")
    public R<Set<String>> getUserButtonPermissions() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserButtonPermissions(loginUser.getId()));
    }

    /**
     * 获取当前用户的按钮权限（按菜单分组）
     * 不需要权限注解，登录用户即可访问
     */
    @GetMapping("/user/buttons-by-menu")
    public R<Map<UUID, Set<String>>> getUserButtonPermissionsByMenu() {
        LoginUser loginUser = currentUserService.getCurrentUser();
        return R.success(menuService.getUserButtonPermissionsByMenu(loginUser.getId()));
    }

    /**
     * 获取所有权限标识列表（用于角色分配权限时选择）
     */
    @GetMapping("/permissions/all")
    @RequiresPermission("system:menu:list")
    public R<List<String>> getAllPermissionCodes() {
        return R.success(menuService.getAllPermissionCodes());
    }
}
