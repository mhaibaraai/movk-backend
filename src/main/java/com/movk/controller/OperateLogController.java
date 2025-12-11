/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.base.result.RCode;
import com.movk.common.enums.BusinessStatus;
import com.movk.dto.log.OperateLogResp;
import com.movk.entity.OperateLog;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.OperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 操作日志管理
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/operlog")
@RequiredArgsConstructor
public class OperateLogController {

    private final OperateLogService operateLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/list")
    @RequiresPermission("monitor:operlog:list")
    public R<Page<OperateLogResp>> list(
        @Parameter(description = "用户ID") @RequestParam(required = false) UUID userId,
        @Parameter(description = "操作模块") @RequestParam(required = false) String module,
        @Parameter(description = "操作类型") @RequestParam(required = false) String operation,
        @Parameter(description = "状态") @RequestParam(required = false) Short status,
        @Parameter(description = "开始时间") @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
        @Parameter(description = "结束时间") @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        BusinessStatus businessStatus = status != null ? BusinessStatus.fromCode(status) : null;

        Page<OperateLog> logPage = operateLogService.listOperateLogs(
            userId, module, operation, businessStatus, startTime, endTime, pageable);

        Page<OperateLogResp> respPage = logPage.map(this::toResp);
        return R.success(respPage);
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{id}")
    @RequiresPermission("monitor:operlog:query")
    public R<OperateLogResp> getInfo(@PathVariable Long id) {
        OperateLog log = operateLogService.getById(id);
        if (log == null) {
            return R.fail(RCode.NOT_FOUND);
        }
        return R.success(toResp(log));
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @RequiresPermission("monitor:operlog:export")
    public R<List<OperateLogResp>> export(
        @Parameter(description = "用户ID") @RequestParam(required = false) UUID userId,
        @Parameter(description = "操作模块") @RequestParam(required = false) String module,
        @Parameter(description = "状态") @RequestParam(required = false) Short status,
        @Parameter(description = "开始时间") @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
        @Parameter(description = "结束时间") @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime
    ) {
        BusinessStatus businessStatus = status != null ? BusinessStatus.fromCode(status) : null;
        List<OperateLog> logs = operateLogService.exportLogs(userId, module, businessStatus, startTime, endTime);
        List<OperateLogResp> respList = logs.stream().map(this::toResp).toList();
        return R.success(respList);
    }

    @Operation(summary = "清理操作日志")
    @DeleteMapping("/clean")
    @RequiresPermission("monitor:operlog:remove")
    public R<Integer> clean(
        @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") int days
    ) {
        int deleted = operateLogService.cleanLogs(days);
        return R.success(deleted);
    }

    /**
     * 实体转 DTO
     */
    private OperateLogResp toResp(OperateLog log) {
        OperateLogResp resp = new OperateLogResp();
        BeanUtils.copyProperties(log, resp);
        return resp;
    }
}
