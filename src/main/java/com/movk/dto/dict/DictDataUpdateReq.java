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

public record DictDataUpdateReq(
    @NotNull(message = "字典数据ID不能为空")
    UUID id,

    @NotBlank(message = "字典类型不能为空")
    @Size(min = 1, max = 100, message = "字典类型长度必须在 1-100 之间")
    String dictType,

    @NotBlank(message = "字典标签不能为空")
    @Size(min = 1, max = 100, message = "字典标签长度必须在 1-100 之间")
    String dictLabel,

    @NotBlank(message = "字典值不能为空")
    @Size(min = 1, max = 100, message = "字典值长度必须在 1-100 之间")
    String dictValue,

    Integer dictSort,

    @Size(max = 100, message = "样式类名长度不能超过 100")
    String cssClass,

    @Size(max = 100, message = "列表样式类名长度不能超过 100")
    String listClass,

    Boolean isDefault,
    EnableStatus status,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
