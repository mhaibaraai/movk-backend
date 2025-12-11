/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dict;

import com.movk.common.enums.EnableStatus;

public record DictTypeCreateReq(
    String dictName,
    String dictType,
    EnableStatus status,
    String remark
) {}
