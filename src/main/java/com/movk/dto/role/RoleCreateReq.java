/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.role;

import com.movk.common.enums.DataScope;
import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * 角色创建请求
 */
public record RoleCreateReq(
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须在 2-50 之间")
    String code,

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在 2-50 之间")
    String name,

    Integer roleSort,
    DataScope dataScope,
    List<UUID> dataScopeDeptIds,
    EnableStatus status,
    RoleType roleType,
    List<UUID> menuIds,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
