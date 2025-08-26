/*
 * @Author yixuanmiao
 * @Date 2025/08/26 22:18
 */

package com.movk.common.web;

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

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.OK.getCode())
                .message(ErrorCode.OK.getMessage())
                .data(data)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
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


