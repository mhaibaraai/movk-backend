/*
 * @Author yixuanmiao
 * @Date 2025/08/27 21:42
 */

package com.movk.adapters.web.support;

public enum ErrorCode {
    OK(0, "OK"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    VALIDATION_FAILED(1001, "Validation failed"),
    BUSINESS_ERROR(1002, "Business error"),
    INTERNAL_ERROR(1000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
