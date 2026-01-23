/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.role.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 创建角色
     */
    UUID createRole(RoleCreateReq req);

    /**
     * 更新角色
     */
    void updateRole(UUID id, RoleUpdateReq req);

    /**
     * 删除角色（逻辑删除）
     */
    void deleteRole(UUID roleId);

    /**
     * 批量删除角色
     */
    void deleteRoles(Iterable<UUID> roleIds);

    /**
     * 根据ID查询角色
     */
    RoleResp getRoleById(UUID roleId);

    /**
     * 查询角色详情（包含菜单信息）
     */
    RoleDetailResp getRoleDetail(UUID roleId);

    /**
     * 分页查询角色列表
     */
    Page<RoleResp> getRolePage(RoleQueryReq queryReq, Pageable pageable);

    /**
     * 分配角色菜单
     */
    void assignMenus(UUID roleId, Iterable<UUID> menuIds);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);
}
