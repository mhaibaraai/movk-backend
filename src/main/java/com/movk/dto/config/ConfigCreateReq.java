/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.config;

import com.movk.common.enums.ConfigType;

public record ConfigCreateReq(
    String configName,
    String configKey,
    String configValue,
    ConfigType configType,
    String remark
) {}
