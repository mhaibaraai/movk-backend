/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.post;

import com.movk.common.enums.EnableStatus;

public record PostCreateReq(
    String postCode,
    String postName,
    Integer orderNum,
    EnableStatus status,
    String remark
) {}
