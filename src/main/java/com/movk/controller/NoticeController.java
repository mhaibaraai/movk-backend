/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.notice.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 通知公告 Controller
 */
@Tag(name = "通知公告管理", description = "通知公告相关接口")
@RestController
@RequestMapping("/api/system/notices")
@RequiredArgsConstructor
@Validated
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 获取通知公告分页列表
     */
    @Operation(summary = "分页查询通知公告", description = "管理端分页查询通知公告列表")
    @GetMapping
    @RequiresPermission("system:notice:list")
    public R<Page<NoticeResp>> getNoticePage(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return R.success(noticeService.getNoticePage(pageable));
    }

    /**
     * 获取已发布的通知公告（前端展示，无需权限）
     */
    @Operation(summary = "获取已发布公告", description = "前端展示用，无需登录权限")
    @GetMapping("/published")
    public R<List<NoticeResp>> getPublishedNotices() {
        return R.success(noticeService.getPublishedNotices());
    }

    /**
     * 获取通知公告详情
     */
    @Operation(summary = "查询公告详情", description = "根据公告ID查询详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:notice:query")
    public R<NoticeResp> getNoticeById(
            @Parameter(description = "公告ID") @PathVariable UUID id
    ) {
        return R.success(noticeService.getNoticeById(id));
    }

    /**
     * 新增通知公告
     */
    @Operation(summary = "新增公告", description = "创建新的通知公告")
    @PostMapping
    @RequiresPermission("system:notice:add")
    @Log(module = "通知公告", operation = CREATE)
    public R<UUID> createNotice(@Valid @RequestBody NoticeCreateReq req) {
        return R.success(noticeService.createNotice(req));
    }

    /**
     * 修改通知公告
     */
    @Operation(summary = "修改公告", description = "修改通知公告内容")
    @PutMapping
    @RequiresPermission("system:notice:edit")
    @Log(module = "通知公告", operation = UPDATE)
    public R<Void> updateNotice(@Valid @RequestBody NoticeUpdateReq req) {
        noticeService.updateNotice(req);
        return R.ok();
    }

    /**
     * 删除通知公告
     */
    @Operation(summary = "删除公告", description = "逻辑删除通知公告")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:notice:delete")
    @Log(module = "通知公告", operation = DELETE)
    public R<Void> deleteNotice(
            @Parameter(description = "公告ID") @PathVariable UUID id
    ) {
        noticeService.deleteNotice(id);
        return R.ok();
    }
}
