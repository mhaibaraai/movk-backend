/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.role;

import com.movk.common.enums.DataScope;
import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.RoleType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RoleResp(
    UUID id,
    String code,
    String name,
    Integer roleSort,
    DataScope dataScope,
    List<UUID> dataScopeDeptIds,
    EnableStatus status,
    RoleType roleType,
    String remark,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
