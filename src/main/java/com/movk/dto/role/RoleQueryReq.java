/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.role;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.RoleType;

import java.time.OffsetDateTime;

public record RoleQueryReq(
    String code,
    String name,
    EnableStatus status,
    RoleType roleType,
    OffsetDateTime createdAtStart,
    OffsetDateTime createdAtEnd
) {}
