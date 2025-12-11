/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.dict;

import com.movk.common.enums.EnableStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DictDataResp(
    UUID id,
    String dictType,
    String dictLabel,
    String dictValue,
    Integer dictSort,
    String cssClass,
    String listClass,
    Boolean isDefault,
    EnableStatus status,
    String remark,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
