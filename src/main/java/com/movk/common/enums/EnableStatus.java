/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.common.enums;

import lombok.Getter;

@Getter
public enum EnableStatus {
    ENABLED((short) 1),
    DISABLED((short) 2);

    private final short code;

    EnableStatus(short code) {
        this.code = code;
    }

    public static EnableStatus fromCode(Short code) {
        if (code == null) {
            return null;
        }
        for (EnableStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown EnableStatus code: " + code);
    }
}
