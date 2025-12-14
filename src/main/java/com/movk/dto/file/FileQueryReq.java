/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件查询请求
 */
@Data
@Schema(description = "文件查询请求")
public class FileQueryReq {

    @Schema(description = "原始文件名（模糊查询）")
    private String originalName;

    @Schema(description = "文件分类")
    private String category;

    @Schema(description = "内容类型（模糊查询）")
    private String contentType;
}
