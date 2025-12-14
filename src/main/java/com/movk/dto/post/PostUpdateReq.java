/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.post;

import com.movk.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PostUpdateReq(
    @NotNull(message = "岗位ID不能为空")
    UUID id,

    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 1, max = 50, message = "岗位名称长度必须在 1-50 之间")
    String postName,

    Integer orderNum,
    EnableStatus status,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
