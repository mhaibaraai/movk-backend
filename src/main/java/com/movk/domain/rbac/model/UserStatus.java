/*
 * @Author yixuanmiao
 * @Date 2025/08/27 21:45
 */

package com.movk.domain.rbac.model;

/**
 * 用户状态
 */
public enum UserStatus {
    ACTIVE((short) 1),
    DISABLED((short) 2),
    LOCKED((short) 3),
    DELETED((short) 4);

    private final short code;

    UserStatus(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
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
