/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 角色类型枚举
 */
@Getter
public enum RoleType {
    BUILT_IN((short) 1, "内置角色"),
    CUSTOM((short) 2, "自定义角色");

    private final short code;
    private final String description;

    RoleType(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RoleType fromCode(Short code) {
        if (code == null) {
            return CUSTOM;
        }
        for (RoleType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown RoleType code: " + code);
    }
}
