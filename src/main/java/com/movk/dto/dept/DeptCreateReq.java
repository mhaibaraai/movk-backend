/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dept;

import com.movk.common.enums.EnableStatus;

import java.util.UUID;

public record DeptCreateReq(
    UUID parentId,
    String deptName,
    String deptCode,
    Integer orderNum,
    UUID leaderUserId,
    String phone,
    String email,
    EnableStatus status
) {}
