/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dict;

import com.movk.common.enums.EnableStatus;

import java.util.UUID;

public record DictDataUpdateReq(
    UUID id,
    String dictType,
    String dictLabel,
    String dictValue,
    Integer dictSort,
    String cssClass,
    String listClass,
    Boolean isDefault,
    EnableStatus status,
    String remark
) {}
