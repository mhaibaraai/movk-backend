/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 业务状态枚举
 */
@Getter
public enum BusinessStatus {
    SUCCESS((short) 1, "成功"),
    FAILURE((short) 2, "失败");

    private final short code;
    private final String description;

    BusinessStatus(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BusinessStatus fromCode(Short code) {
        if (code == null) {
            return FAILURE;
        }
        for (BusinessStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown BusinessStatus code: " + code);
    }
}
