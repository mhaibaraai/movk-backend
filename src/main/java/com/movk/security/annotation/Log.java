/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.annotation;

import com.movk.common.enums.OperationType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标注需要记录操作日志的方法
 *
 * <p>使用示例：
 * <pre>
 * &#64;Log(module = "用户管理", operation = OperationType.CREATE)
 * public User createUser(UserCreateReq req) { ... }
 *
 * &#64;Log(module = "用户管理", operation = OperationType.DELETE, isSaveRequestData = false)
 * public void deleteUser(UUID id) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块
     * 如：用户管理、角色管理、菜单管理等
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperationType operation() default OperationType.OTHER;

    /**
     * 操作描述
     * 可选，用于补充说明操作内容
     */
    String description() default "";

    /**
     * 是否保存请求参数
     * 默认 true，对于包含敏感信息的接口可设置为 false
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应数据
     * 默认 true，对于返回大量数据的接口可设置为 false
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除的请求参数字段
     * 用于脱敏，如 password, token 等敏感字段
     */
    String[] excludeParamNames() default {"password", "oldPassword", "newPassword", "confirmPassword"};
}
