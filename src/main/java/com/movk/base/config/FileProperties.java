/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * 文件存储根路径
     */
    private String basePath = "./uploads";

    /**
     * 最大文件大小（字节），默认 50MB
     */
    private Long maxSize = 50 * 1024 * 1024L;

    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions = Arrays.asList(
            // 图片
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico",
            // 文档
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "md",
            // 压缩包
            "zip", "rar", "7z", "tar", "gz",
            // 其他
            "json", "xml", "csv"
    );

    /**
     * 是否按日期分目录存储
     */
    private Boolean datePathEnabled = true;

    /**
     * 存储类型：local-本地存储
     */
    private String storageType = "local";
}
