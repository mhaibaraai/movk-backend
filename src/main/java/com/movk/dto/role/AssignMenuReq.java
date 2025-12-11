/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.role;

import java.util.List;
import java.util.UUID;

public record AssignMenuReq(
    UUID roleId,
    List<UUID> menuIds
) {}
