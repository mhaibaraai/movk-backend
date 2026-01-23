/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dept;

import com.movk.common.enums.EnableStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DeptUpdateReq(
    UUID parentId,

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 1, max = 50, message = "部门名称长度必须在 1-50 之间")
    String deptName,

    @Size(max = 50, message = "部门编码长度不能超过 50")
    String deptCode,

    Integer orderNum,
    UUID leaderUserId,

    @Size(max = 20, message = "联系电话长度不能超过 20")
    String phone,

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    String email,

    EnableStatus status
) {}
