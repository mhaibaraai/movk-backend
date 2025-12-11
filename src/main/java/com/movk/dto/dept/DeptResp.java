/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dept;

import com.movk.common.enums.EnableStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DeptResp(
    UUID id,
    UUID parentId,
    String deptName,
    String deptCode,
    Integer orderNum,
    UUID leaderUserId,
    String leaderUserName,
    String phone,
    String email,
    EnableStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<DeptResp> children
) {}
