/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标注需要进行数据权限过滤的方法
 *
 * <p>数据权限范围说明：
 * <ul>
 *   <li>ALL - 全部数据，不做任何限制</li>
 *   <li>DEPT - 仅本部门数据</li>
 *   <li>DEPT_AND_CHILD - 本部门及子部门数据</li>
 *   <li>SELF - 仅本人数据</li>
 *   <li>CUSTOM - 自定义部门数据权限</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * &#64;DataPermission(deptAlias = "d", userAlias = "u")
 * public List&lt;User&gt; getUserList() { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 部门表的别名
     * 用于 SQL 拼接时指定部门表别名
     */
    String deptAlias() default "";

    /**
     * 用户表的别名
     * 用于 SQL 拼接时指定用户表别名（用于"仅本人数据"场景）
     */
    String userAlias() default "";

    /**
     * 部门 ID 字段名
     * 默认为 dept_id
     */
    String deptIdColumn() default "dept_id";

    /**
     * 用户 ID 字段名
     * 默认为 creator（创建人字段，用于"仅本人数据"场景）
     */
    String userIdColumn() default "creator";
}
