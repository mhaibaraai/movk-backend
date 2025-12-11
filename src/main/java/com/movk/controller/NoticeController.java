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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 通知公告 Controller
 */
@RestController
@RequestMapping("/api/system/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 获取通知公告分页列表
     */
    @GetMapping("/page")
    @RequiresPermission("system:notice:list")
    public R<Page<NoticeResp>> getNoticePage(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return R.success(noticeService.getNoticePage(pageable));
    }

    /**
     * 获取已发布的通知公告（前端展示，无需权限）
     */
    @GetMapping("/published")
    public R<List<NoticeResp>> getPublishedNotices() {
        return R.success(noticeService.getPublishedNotices());
    }

    /**
     * 获取通知公告详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:notice:query")
    public R<NoticeResp> getNoticeById(@PathVariable UUID id) {
        return R.success(noticeService.getNoticeById(id));
    }

    /**
     * 新增通知公告
     */
    @PostMapping
    @RequiresPermission("system:notice:add")
    @Log(module = "通知公告", operation = CREATE)
    public R<UUID> createNotice(@RequestBody NoticeCreateReq req) {
        return R.success(noticeService.createNotice(req));
    }

    /**
     * 修改通知公告
     */
    @PutMapping
    @RequiresPermission("system:notice:edit")
    @Log(module = "通知公告", operation = UPDATE)
    public R<Void> updateNotice(@RequestBody NoticeUpdateReq req) {
        noticeService.updateNotice(req);
        return R.ok();
    }

    /**
     * 删除通知公告
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("system:notice:delete")
    @Log(module = "通知公告", operation = DELETE)
    public R<Void> deleteNotice(@PathVariable UUID id) {
        noticeService.deleteNotice(id);
        return R.ok();
    }
}
