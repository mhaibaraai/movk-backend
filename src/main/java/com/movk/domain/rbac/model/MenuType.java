/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.domain.rbac.model;

import lombok.Getter;

@Getter
public enum MenuType {
    DIRECTORY((short) 1),
    MENU((short) 2),
    BUTTON((short) 3);

    private final short code;

    MenuType(short code) {
        this.code = code;
    }

    public static MenuType fromCode(Short code) {
        if (code == null) {
            return null;
        }
        for (MenuType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown MenuType code: " + code);
    }
}