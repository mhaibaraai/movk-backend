/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.config;

import com.movk.common.enums.ConfigType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConfigUpdateReq(
    @NotNull(message = "配置ID不能为空")
    UUID id,

    @NotBlank(message = "配置名称不能为空")
    @Size(min = 1, max = 100, message = "配置名称长度必须在 1-100 之间")
    String configName,

    @NotBlank(message = "配置键不能为空")
    @Size(min = 1, max = 100, message = "配置键长度必须在 1-100 之间")
    String configKey,

    @Size(max = 500, message = "配置值长度不能超过 500")
    String configValue,

    ConfigType configType,

    @Size(max = 500, message = "备注长度不能超过 500")
    String remark
) {}
