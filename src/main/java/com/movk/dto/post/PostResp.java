/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.post;

import com.movk.common.enums.EnableStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PostResp(
    UUID id,
    String postCode,
    String postName,
    Integer orderNum,
    EnableStatus status,
    String remark,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
