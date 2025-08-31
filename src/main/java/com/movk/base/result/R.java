/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.base.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;

    /**
     * 构造成功响应
     */
    public static <T> R<T> success(T data) {
        return R.<T>builder()
                .code(RCode.OK.getCode())
                .message(RCode.OK.getMessage())
                .data(data)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造成功响应（无数据）
     */
    public static <T> R<T> ok() {
        return success(null);
    }

    /**
     * 构造错误响应
     */
    public static <T> R<T> error(RCode errorCode, String message) {
        // 对空入参进行回退，优先使用调用者提供的文案，否则使用错误码默认文案
        String resolvedMessage = (message == null || message.isEmpty()) ? errorCode.getMessage() : message;
        return R.<T>builder()
                .code(errorCode.getCode())
                .message(resolvedMessage)
                .data(null)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造错误响应（使用错误码默认消息）
     */
    public static <T> R<T> fail(RCode errorCode) {
        return error(errorCode, null);
    }
}
