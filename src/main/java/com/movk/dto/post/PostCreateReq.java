/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.post;

import com.movk.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateReq(
    @NotBlank(message = "岗位编码不能为空")
    @Size(min = 1, max = 50, message = "岗位编码长度必须在 1-50 之间")
    String postCode,

    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 1, max = 50, message = "岗位名称长度必须在 1-50 之间")
    String postName,

    Integer orderNum,
    EnableStatus status,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
