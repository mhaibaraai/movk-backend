/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import com.movk.common.enums.Gender;
import com.movk.common.enums.UserStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserDetailResp(
    UUID id,
    String username,
    String nickname,
    String email,
    String phone,
    Gender gender,
    String avatar,
    UserStatus status,
    UUID deptId,
    String deptName,
    List<UUID> roleIds,
    List<String> roleCodes,
    List<String> roleNames,
    List<UUID> postIds,
    List<String> postCodes,
    List<String> postNames,
    String loginIp,
    OffsetDateTime loginDate,
    String remark,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
