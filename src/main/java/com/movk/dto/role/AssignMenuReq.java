/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.role;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignMenuReq(
    @NotNull(message = "角色ID不能为空")
    UUID roleId,

    @NotNull(message = "菜单ID列表不能为空")
    List<UUID> menuIds
) {}
