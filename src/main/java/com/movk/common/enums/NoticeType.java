/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
public enum NoticeType {
    NOTICE((short) 1, "通知"),
    ANNOUNCEMENT((short) 2, "公告");

    private final short code;
    private final String description;

    NoticeType(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static NoticeType fromCode(Short code) {
        if (code == null) {
            return NOTICE;
        }
        for (NoticeType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown NoticeType code: " + code);
    }
}
