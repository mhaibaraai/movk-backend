/*
 * @Author yixuanmiao
 * @Date 2025/08/28
 */

package com.movk.adapters.web.support;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ApiRCode code;

    public BusinessException(ApiRCode code, String message) {
        super((message == null || message.isEmpty()) ? code.getMessage() : message);
        this.code = code;
    }

    public BusinessException(ApiRCode code) {
        this(code, null);
    }

    // example
    public static void main(String[] args) {
        try {
            throw new BusinessException(ApiRCode.INTERNAL_ERROR, "服务器内部错误");
        } catch (BusinessException e) {
            System.out.println("捕获到业务异常:");
            System.out.println("code: " + e.getCode().getCode());
            System.out.println("message: " + e.getMessage());
        }
    }
}


