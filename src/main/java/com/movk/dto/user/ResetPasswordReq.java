/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ResetPasswordReq(
    @NotNull(message = "用户ID不能为空")
    UUID userId,

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在 6-50 之间")
    String newPassword
) {}
