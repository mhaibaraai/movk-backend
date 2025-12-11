/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.controller.vo;

import com.movk.dto.menu.MenuTreeResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 用户信息视图对象
 * 用于返回当前登录用户的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    /**
     * 用户ID
     */
    private UUID id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 显示名称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户角色代码列表
     */
    private List<String> roles;

    /**
     * 菜单权限树
     */
    private List<MenuTreeResp> menus;

    /**
     * 按钮权限代码列表
     */
    private List<String> permissions;
}
