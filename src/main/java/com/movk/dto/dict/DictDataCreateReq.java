/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dict;

import com.movk.common.enums.EnableStatus;

public record DictDataCreateReq(
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
