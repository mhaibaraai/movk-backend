/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.post;

import com.movk.common.enums.EnableStatus;

import java.util.UUID;

public record PostUpdateReq(
    UUID id,
    String postName,
    Integer orderNum,
    EnableStatus status,
    String remark
) {}
