/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.config.CacheConfig;
import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.MenuType;
import com.movk.dto.menu.*;
import com.movk.entity.Menu;
import com.movk.entity.Role;
import com.movk.repository.MenuRepository;
import com.movk.repository.RoleRepository;
import com.movk.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.USER_MENUS, allEntries = true)
    public UUID createMenu(MenuCreateReq req) {
        Menu menu = Menu.builder()
            .parentId(req.parentId())
            .type(req.type())
            .name(req.name())
            .orderNum(req.orderNum())
            .path(req.path())
            .component(req.component())
            .queryParams(req.queryParams())
            .isFrame(req.isFrame() != null ? req.isFrame() : false)
            .isCache(req.isCache() != null ? req.isCache() : true)
            .permissionCode(req.permissionCode())
            .visible(req.visible() != null ? req.visible() : true)
            .status(req.status())
            .icon(req.icon())
            .remark(req.remark())
            .build();

        menu = menuRepository.save(menu);
        return menu.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.USER_MENUS, allEntries = true)
    public void updateMenu(UUID id, MenuUpdateReq req) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "菜单不存在"));

        menu.setParentId(req.parentId());
        menu.setType(req.type());
        menu.setName(req.name());
        menu.setOrderNum(req.orderNum());
        menu.setPath(req.path());
        menu.setComponent(req.component());
        menu.setQueryParams(req.queryParams());
        menu.setIsFrame(req.isFrame());
        menu.setIsCache(req.isCache());
        menu.setPermissionCode(req.permissionCode());
        menu.setVisible(req.visible());
        menu.setStatus(req.status());
        menu.setIcon(req.icon());
        menu.setRemark(req.remark());

        menuRepository.save(menu);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.USER_MENUS, allEntries = true)
    public void deleteMenu(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "菜单不存在"));

        List<Menu> children = menuRepository.findByParentIdAndDeletedFalseOrderByOrderNumAsc(menuId);
        if (!children.isEmpty()) {
            throw new BusinessException(RCode.BAD_REQUEST, "存在子菜单，无法删除");
        }

        menu.setDeleted(true);
        menu.setDeletedAt(OffsetDateTime.now());
        menuRepository.save(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResp getMenuById(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "菜单不存在"));

        return toMenuResp(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResp> getAllMenus() {
        List<Menu> menus = menuRepository.findByDeletedFalseOrderByOrderNumAsc();
        return menus.stream()
            .map(this::toMenuResp)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResp> getMenuTree() {
        List<Menu> allMenus = menuRepository.findByDeletedFalseOrderByOrderNumAsc();
        return buildMenuTree(allMenus, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_MENUS, key = "'tree:' + #userId", unless = "#result.isEmpty()")
    public List<MenuTreeResp> getUserMenuTree(UUID userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of();
        }

        List<String> roleCodes = roles.stream()
            .map(Role::getCode)
            .collect(Collectors.toList());

        List<Menu> menus = menuRepository.findByRoleCodesAndStatusAndVisible(
            roleCodes,
            EnableStatus.ENABLED,
            true
        );

        menus = menus.stream()
            .filter(m -> m.getType() == MenuType.DIRECTORY || m.getType() == MenuType.MENU)
            .collect(Collectors.toList());

        return buildMenuTreeResp(menus, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(UUID userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of();
        }

        List<String> roleCodes = roles.stream()
            .map(Role::getCode)
            .collect(Collectors.toList());

        return menuRepository.findPermissionCodesByRoleCodesAndStatus(
            roleCodes,
            EnableStatus.ENABLED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResp> getMenusByRoleIds(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        List<Role> roles = roleRepository.findAllById(roleIds);
        List<String> roleCodes = roles.stream()
            .map(Role::getCode)
            .collect(Collectors.toList());

        List<Menu> menus = menuRepository.findByRoleCodesAndStatus(
            roleCodes,
            EnableStatus.ENABLED
        );

        return menus.stream()
            .map(this::toMenuResp)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getUserButtonPermissions(UUID userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return Set.of();
        }

        List<String> roleCodes = roles.stream()
            .map(Role::getCode)
            .collect(Collectors.toList());

        // 获取所有按钮类型的菜单
        List<Menu> buttonMenus = menuRepository.findByRoleCodesAndStatus(
            roleCodes,
            EnableStatus.ENABLED
        );

        return buttonMenus.stream()
            .filter(m -> m.getType() == MenuType.BUTTON)
            .map(Menu::getPermissionCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Set<String>> getUserButtonPermissionsByMenu(UUID userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return Map.of();
        }

        List<String> roleCodes = roles.stream()
            .map(Role::getCode)
            .collect(Collectors.toList());

        List<Menu> buttonMenus = menuRepository.findByRoleCodesAndStatus(
            roleCodes,
            EnableStatus.ENABLED
        );

        // 按父菜单ID分组按钮权限
        return buttonMenus.stream()
            .filter(m -> m.getType() == MenuType.BUTTON && m.getPermissionCode() != null)
            .collect(Collectors.groupingBy(
                Menu::getParentId,
                Collectors.mapping(Menu::getPermissionCode, Collectors.toSet())
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllPermissionCodes() {
        List<Menu> menus = menuRepository.findByDeletedFalseOrderByOrderNumAsc();
        return menus.stream()
            .map(Menu::getPermissionCode)
            .filter(Objects::nonNull)
            .filter(code -> !code.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }

    private MenuResp toMenuResp(Menu menu) {
        return new MenuResp(
            menu.getId(),
            menu.getParentId(),
            menu.getType(),
            menu.getName(),
            menu.getOrderNum(),
            menu.getPath(),
            menu.getComponent(),
            menu.getQueryParams(),
            menu.getIsFrame(),
            menu.getIsCache(),
            menu.getPermissionCode(),
            menu.getVisible(),
            menu.getStatus(),
            menu.getIcon(),
            menu.getRemark(),
            menu.getCreatedAt(),
            menu.getUpdatedAt(),
            null
        );
    }

    private List<MenuResp> buildMenuTree(List<Menu> allMenus, UUID parentId) {
        return allMenus.stream()
            .filter(menu -> Objects.equals(menu.getParentId(), parentId))
            .map(menu -> {
                List<MenuResp> children = buildMenuTree(allMenus, menu.getId());
                return new MenuResp(
                    menu.getId(),
                    menu.getParentId(),
                    menu.getType(),
                    menu.getName(),
                    menu.getOrderNum(),
                    menu.getPath(),
                    menu.getComponent(),
                    menu.getQueryParams(),
                    menu.getIsFrame(),
                    menu.getIsCache(),
                    menu.getPermissionCode(),
                    menu.getVisible(),
                    menu.getStatus(),
                    menu.getIcon(),
                    menu.getRemark(),
                    menu.getCreatedAt(),
                    menu.getUpdatedAt(),
                    children.isEmpty() ? null : children
                );
            })
            .collect(Collectors.toList());
    }

    private List<MenuTreeResp> buildMenuTreeResp(List<Menu> allMenus, UUID parentId) {
        return allMenus.stream()
            .filter(menu -> Objects.equals(menu.getParentId(), parentId))
            .map(menu -> {
                List<MenuTreeResp> children = buildMenuTreeResp(allMenus, menu.getId());
                return new MenuTreeResp(
                    menu.getId(),
                    menu.getParentId(),
                    menu.getName(),
                    menu.getPath(),
                    menu.getComponent(),
                    menu.getIcon(),
                    menu.getVisible(),
                    menu.getOrderNum(),
                    children.isEmpty() ? null : children
                );
            })
            .collect(Collectors.toList());
    }
}
