/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 登录类型枚举
 */
@Getter
public enum LoginType {
    PASSWORD((short) 1, "账号密码登录"),
    SMS((short) 2, "手机验证码登录"),
    OAUTH((short) 3, "第三方登录"),
    LOGOUT((short) 4, "用户登出");

    private final short code;
    private final String description;

    LoginType(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LoginType fromCode(Short code) {
        if (code == null) {
            return PASSWORD;
        }
        for (LoginType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown LoginType code: " + code);
    }
}
