/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.base.exception;

import com.movk.base.result.RCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final RCode code;

    public BusinessException(RCode code, String message) {
        super((message == null || message.isEmpty()) ? code.getMessage() : message);
        this.code = code;
    }

    public BusinessException(RCode code) {
        this(code, null);
    }

    // 无示例主方法，避免在生产代码中残留样例
}


