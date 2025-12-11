/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum Gender {
    UNKNOWN((short) 0, "未知"),
    MALE((short) 1, "男"),
    FEMALE((short) 2, "女");

    private final short code;
    private final String description;

    Gender(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Gender fromCode(Short code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (Gender value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown Gender code: " + code);
    }
}
