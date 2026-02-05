/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.file.FileQueryReq;
import com.movk.dto.file.FileResp;
import com.movk.dto.file.FileUploadResp;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 文件管理 Controller
 */
@Tag(name = "文件管理", description = "文件上传、下载、查询等接口")
@RestController
@RequestMapping("/${api.version}/system/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    /**
     * 上传单个文件
     */
    @Operation(summary = "上传文件", description = "上传单个文件，返回文件信息")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresPermission("system:file:create")
    @Log(module = "文件管理", operation = CREATE)
    public R<FileUploadResp> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件分类") @RequestParam(value = "category", required = false) String category
    ) {
        return R.success(fileService.upload(file, category));
    }

    /**
     * 批量上传文件
     */
    @Operation(summary = "批量上传文件", description = "一次上传多个文件")
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresPermission("system:file:create")
    @Log(module = "文件管理", operation = CREATE)
    public R<List<FileUploadResp>> uploadBatch(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件分类") @RequestParam(value = "category", required = false) String category
    ) {
        return R.success(fileService.uploadBatch(files, category));
    }

    /**
     * 下载文件
     */
    @Operation(summary = "下载文件", description = "根据文件 ID 下载文件")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Resource resource = fileService.download(id);
        String originalName = fileService.getOriginalName(id);
        String contentType = fileService.getContentType(id);

        String encodedFilename = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    /**
     * 预览文件（在线查看，不触发下载）
     */
    @Operation(summary = "预览文件", description = "在线预览文件，适用于图片、PDF 等")
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> preview(@PathVariable UUID id) {
        Resource resource = fileService.download(id);
        String contentType = fileService.getContentType(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * 获取文件详情
     */
    @Operation(summary = "获取文件详情", description = "根据文件 ID 获取文件详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:file:query")
    public R<FileResp> getFileInfo(@PathVariable UUID id) {
        return R.success(fileService.getFileInfo(id));
    }

    /**
     * 分页查询文件列表
     */
    @Operation(summary = "查询文件列表", description = "分页查询文件列表")
    @GetMapping
    @RequiresPermission("system:file:list")
    public R<Page<FileResp>> getFilePage(
            FileQueryReq query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return R.success(fileService.getFilePage(query, pageable));
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "根据文件 ID 删除文件")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:file:delete")
    @Log(module = "文件管理", operation = DELETE)
    public R<Void> delete(@PathVariable UUID id) {
        fileService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除文件
     */
    @Operation(summary = "批量删除文件", description = "批量删除文件")
    @DeleteMapping("/batch")
    @RequiresPermission("system:file:delete")
    @Log(module = "文件管理", operation = DELETE)
    public R<Void> deleteBatch(@RequestBody @NotEmpty(message = "文件 ID 列表不能为空") List<UUID> ids) {
        fileService.deleteBatch(ids);
        return R.ok();
    }
}
