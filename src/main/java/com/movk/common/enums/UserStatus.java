/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 用户状态
 */
@Getter
public enum UserStatus {
    ACTIVE((short) 1),
    DISABLED((short) 2),
    LOCKED((short) 3),
    DELETED((short) 4);

    private final short code;

    UserStatus(short code) {
        this.code = code;
    }

    public static UserStatus fromCode(Short code) {
        if (code == null) {
            return null;
        }
        for (UserStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus code: " + code);
    }
}
