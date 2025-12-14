/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignRoleReq(
    @NotNull(message = "用户ID不能为空")
    UUID userId,

    @NotNull(message = "角色ID列表不能为空")
    List<UUID> roleIds
) {}
