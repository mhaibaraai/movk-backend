/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import com.movk.common.enums.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserQueryReq(
    String username,
    String nickname,
    String phone,
    String email,
    UserStatus status,
    UUID deptId,
    OffsetDateTime createdAtStart,
    OffsetDateTime createdAtEnd
) {}
