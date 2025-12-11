/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import java.util.List;
import java.util.UUID;

public record AssignRoleReq(
    UUID userId,
    List<UUID> roleIds
) {}
