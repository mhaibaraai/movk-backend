/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 配置类型枚举
 */
@Getter
public enum ConfigType {
    BUILTIN((short) 1, "系统内置"),
    CUSTOM((short) 2, "自定义");

    private final short code;
    private final String description;

    ConfigType(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ConfigType fromCode(Short code) {
        if (code == null) {
            return BUILTIN;
        }
        for (ConfigType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ConfigType code: " + code);
    }
}
