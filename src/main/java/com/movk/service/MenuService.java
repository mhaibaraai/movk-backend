/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.menu.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 创建菜单
     */
    UUID createMenu(MenuCreateReq req);

    /**
     * 更新菜单
     */
    void updateMenu(UUID id, MenuUpdateReq req);

    /**
     * 删除菜单（逻辑删除）
     */
    void deleteMenu(UUID menuId);

    /**
     * 根据ID查询菜单
     */
    MenuResp getMenuById(UUID menuId);

    /**
     * 查询所有菜单列表
     */
    List<MenuResp> getAllMenus();

    /**
     * 查询菜单树
     */
    List<MenuResp> getMenuTree();

    /**
     * 根据用户ID查询菜单树
     */
    List<MenuTreeResp> getUserMenuTree(UUID userId);

    /**
     * 根据用户ID查询权限标识列表
     */
    List<String> getUserPermissions(UUID userId);

    /**
     * 根据角色ID列表查询菜单列表
     */
    List<MenuResp> getMenusByRoleIds(List<UUID> roleIds);

    /**
     * 根据用户ID查询按钮权限集合
     * 返回格式：Set<"system:user:add", "system:user:edit", ...>
     */
    Set<String> getUserButtonPermissions(UUID userId);

    /**
     * 根据用户ID查询按钮权限（按菜单分组）
     * 返回格式：Map<menuId, Set<permissionCode>>
     */
    Map<UUID, Set<String>> getUserButtonPermissionsByMenu(UUID userId);

    /**
     * 查询所有权限标识列表
     */
    List<String> getAllPermissionCodes();
}
