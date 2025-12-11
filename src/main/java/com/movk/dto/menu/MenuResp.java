/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.menu;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.MenuType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MenuResp(
    UUID id,
    UUID parentId,
    MenuType type,
    String name,
    Integer orderNum,
    String path,
    String component,
    String queryParams,
    Boolean isFrame,
    Boolean isCache,
    String permissionCode,
    Boolean visible,
    EnableStatus status,
    String icon,
    String remark,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<MenuResp> children
) {}
