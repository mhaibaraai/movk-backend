/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dict;

import com.movk.common.enums.EnableStatus;

import java.util.UUID;

public record DictTypeUpdateReq(
    UUID id,
    String dictName,
    String dictType,
    EnableStatus status,
    String remark
) {}
