/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import java.util.UUID;

public record UpdatePasswordReq(
    UUID userId,
    String oldPassword,
    String newPassword
) {}
