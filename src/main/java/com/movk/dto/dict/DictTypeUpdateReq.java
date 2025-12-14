/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dict;

import com.movk.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DictTypeUpdateReq(
    @NotNull(message = "字典类型ID不能为空")
    UUID id,

    @NotBlank(message = "字典名称不能为空")
    @Size(min = 1, max = 100, message = "字典名称长度必须在 1-100 之间")
    String dictName,

    @NotBlank(message = "字典类型不能为空")
    @Size(min = 1, max = 100, message = "字典类型长度必须在 1-100 之间")
    String dictType,

    EnableStatus status,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
