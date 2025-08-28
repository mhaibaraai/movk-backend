/*
 * @Author yixuanmiao
 * @Date 2025/08/28 11:19
 */

package com.movk.adapters.web.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;

    /**
     * 构造成功响应。
     *
     * @param data 成功返回的数据载荷
     * @param <T>  数据泛型
     * @return 标准成功响应：业务码为 {@code OK}，自动从 MDC 读取 {@code traceId}
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ApiRCode.OK.getCode())
                .message(ApiRCode.OK.getMessage())
                .data(data)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造错误响应。
     *
     * @param errorCode 业务错误码
     * @param message   可选的覆盖文案；为空或空串时回退为 {@code errorCode} 默认文案
     * @param <T>       数据泛型（错误时通常为空）
     * @return 标准错误响应：根据错误码设置 {@code code} 与 {@code message}
     */
    public static <T> ApiResponse<T> error(ApiRCode errorCode, String message) {
        // 对空入参进行回退，优先使用调用者提供的文案，否则使用错误码默认文案
        String resolvedMessage = (message == null || message.isEmpty()) ? errorCode.getMessage() : message;
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(resolvedMessage)
                .data(null)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
