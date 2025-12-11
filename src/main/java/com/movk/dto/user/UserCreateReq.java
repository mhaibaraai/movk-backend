/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.user;

import com.movk.common.enums.Gender;
import com.movk.common.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * 用户创建请求
 */
public record UserCreateReq(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 30, message = "用户名长度必须在 2-30 之间")
    String username,

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在 6-50 之间")
    String password,

    @Size(max = 50, message = "昵称长度不能超过 50")
    String nickname,

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    String email,

    @Size(max = 20, message = "手机号长度不能超过 20")
    String phone,

    Gender gender,
    String avatar,
    UserStatus status,
    UUID deptId,
    List<UUID> roleIds,
    List<UUID> postIds,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
