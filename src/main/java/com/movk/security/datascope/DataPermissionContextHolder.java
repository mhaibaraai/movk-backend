/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.datascope;

/**
 * 数据权限上下文持有者
 * 使用 ThreadLocal 存储当前请求的数据权限上下文
 */
public class DataPermissionContextHolder {

    private static final ThreadLocal<DataPermissionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private DataPermissionContextHolder() {
        // 私有构造函数，防止实例化
    }

    /**
     * 设置数据权限上下文
     */
    public static void setContext(DataPermissionContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取数据权限上下文
     */
    public static DataPermissionContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除数据权限上下文
     * 必须在请求结束时调用，防止内存泄漏
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 检查是否存在数据权限上下文
     */
    public static boolean hasContext() {
        return CONTEXT_HOLDER.get() != null;
    }
}
