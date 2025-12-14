/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 文件信息响应
 */
@Data
@Builder
@Schema(description = "文件信息响应")
public class FileResp {

    @Schema(description = "文件 ID")
    private UUID id;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "存储文件名")
    private String storageName;

    @Schema(description = "文件扩展名")
    private String extension;

    @Schema(description = "文件 MIME 类型")
    private String contentType;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "文件大小（格式化）")
    private String sizeFormatted;

    @Schema(description = "文件路径")
    private String path;

    @Schema(description = "文件 MD5")
    private String md5;

    @Schema(description = "存储类型")
    private String storageType;

    @Schema(description = "文件分类")
    private String category;

    @Schema(description = "下载 URL")
    private String downloadUrl;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "备注")
    private String remark;
}
