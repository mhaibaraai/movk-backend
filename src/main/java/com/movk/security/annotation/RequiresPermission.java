/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标注需要特定权限才能访问的方法或类
 *
 * <p>使用示例：
 * <pre>
 * // 单个权限
 * &#64;RequiresPermission("system:user:list")
 *
 * // 多个权限 - 满足任意一个即可
 * &#64;RequiresPermission(value = {"system:user:list", "system:user:query"}, logical = Logical.OR)
 *
 * // 多个权限 - 必须全部满足
 * &#64;RequiresPermission(value = {"system:user:list", "system:user:query"}, logical = Logical.AND)
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 权限标识数组
     * 权限标识格式建议：模块:资源:操作，如 system:user:list
     */
    String[] value();

    /**
     * 多个权限之间的逻辑关系
     * 默认为 OR，即满足任意一个权限即可
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 与 - 必须同时拥有所有权限
         */
        AND,

        /**
         * 或 - 拥有任意一个权限即可
         */
        OR
    }
}
