/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.config;

import com.movk.common.enums.ConfigType;

import java.util.UUID;

public record ConfigUpdateReq(
    UUID id,
    String configName,
    String configKey,
    String configValue,
    ConfigType configType,
    String remark
) {}
