/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 数据权限范围枚举
 */
@Getter
public enum DataScope {
    ALL((short) 1, "全部数据权限"),
    DEPT((short) 2, "本部门数据权限"),
    DEPT_AND_CHILD((short) 3, "本部门及子部门数据权限"),
    SELF((short) 4, "仅本人数据权限"),
    CUSTOM((short) 5, "自定义部门数据权限");

    private final short code;
    private final String description;

    DataScope(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DataScope fromCode(Short code) {
        if (code == null) {
            return ALL;
        }
        for (DataScope value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown DataScope code: " + code);
    }
}
