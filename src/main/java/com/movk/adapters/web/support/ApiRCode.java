/*
 * @Author yixuanmiao
 * @Date 2025/08/28 21:23
 */

package com.movk.adapters.web.support;

import lombok.Getter;

@Getter
public enum ApiRCode {

    OK(0, "成功"),

    // 基础类
    VALIDATION_FAILED(10001, "参数校验失败"),
    BAD_REQUEST(10002, "无效的请求"),
    NOT_FOUND(10003, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(10004, "不支持的请求方法"),
    UNSUPPORTED_MEDIA_TYPE(10005, "不支持的媒体类型"),
    // 权限认证类
    UNAUTHORIZED(10101, "用户未认证（未登录）"),
    FORBIDDEN(10102, "访问被拒绝，权限不足"),
    TOKEN_INVALID(10103, "无效的访问令牌"),
    TOKEN_EXPIRED(10104, "访问令牌已过期"),
    // 其他客户端错误
    RATE_LIMIT_EXCEEDED(10201, "请求频率过高，请稍后再试"),
    RESOURCE_CONFLICT(10202, "资源冲突（例如：尝试创建已存在的资源）"),


    INTERNAL_ERROR(20001, "服务器内部错误，请联系管理员"),
    SERVICE_UNAVAILABLE(20002, "服务暂不可用，请稍后重试"),
    REMOTE_SERVICE_ERROR(20003, "远程服务调用异常"),
    DATABASE_ERROR(20004, "数据库操作异常"),


    BUSINESS_ERROR(30001, "业务执行出错"),
    USER_NOT_FOUND(30101, "用户不存在");


    private final int code;
    private final String message;

    ApiRCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
