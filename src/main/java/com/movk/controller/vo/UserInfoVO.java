/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.controller.vo;

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
    private String displayName;

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
     * 菜单权限列表
     */
    private List<MenuVO> menus;

    /**
     * 按钮权限代码列表
     */
    private List<String> permissions;

    /**
     * 菜单视图对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuVO {

        /**
         * 菜单ID
         */
        private UUID id;

        /**
         * 菜单代码
         */
        private String code;

        /**
         * 菜单名称
         */
        private String name;

        /**
         * 菜单路径
         */
        private String path;

        /**
         * 菜单图标
         */
        private String icon;

        /**
         * 父菜单ID
         */
        private UUID parentId;

        /**
         * 菜单排序
         */
        private Integer sort;

        /**
         * 菜单类型
         */
        private String type;

        /**
         * 子菜单列表
         */
        private List<MenuVO> children;
    }
}
