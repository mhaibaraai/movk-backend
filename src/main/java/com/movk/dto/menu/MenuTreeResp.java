/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.menu;

import java.util.List;
import java.util.UUID;

public record MenuTreeResp(
    UUID id,
    UUID parentId,
    String name,
    String path,
    String component,
    String icon,
    Boolean visible,
    Integer orderNum,
    List<MenuTreeResp> children
) {}
