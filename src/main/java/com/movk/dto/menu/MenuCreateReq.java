/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.menu;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * 菜单创建请求
 */
public record MenuCreateReq(
    UUID parentId,

    @NotNull(message = "菜单类型不能为空")
    MenuType type,

    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 1, max = 50, message = "菜单名称长度必须在 1-50 之间")
    String name,

    Integer orderNum,

    @Size(max = 200, message = "路由地址长度不能超过 200")
    String path,

    @Size(max = 255, message = "组件路径长度不能超过 255")
    String component,

    @Size(max = 255, message = "路由参数长度不能超过 255")
    String queryParams,

    Boolean isFrame,
    Boolean isCache,

    @Size(max = 100, message = "权限标识长度不能超过 100")
    String permissionCode,

    Boolean visible,
    EnableStatus status,

    @Size(max = 100, message = "图标长度不能超过 100")
    String icon,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
