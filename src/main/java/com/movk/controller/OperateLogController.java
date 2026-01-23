/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.base.result.RCode;
import com.movk.common.enums.BusinessStatus;
import com.movk.dto.log.OperateLogQuery;
import com.movk.dto.log.OperateLogResp;
import com.movk.entity.OperateLog;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.OperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志管理
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/api/monitor/operate-logs")
@RequiredArgsConstructor
@Validated
public class OperateLogController {

    private final OperateLogService operateLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    @RequiresPermission("monitor:operateLog:list")
    public R<Page<OperateLogResp>> list(
            OperateLogQuery query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        BusinessStatus status = query.getStatus() != null ? BusinessStatus.fromCode(query.getStatus()) : null;
        Page<OperateLog> logPage = operateLogService.listOperateLogs(
                query.getUserId(), query.getModule(), query.getOperation(), status,
                query.getStartTime(), query.getEndTime(), pageable);
        return R.success(logPage.map(this::toResp));
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{id}")
    @RequiresPermission("monitor:operateLog:query")
    public R<OperateLogResp> getById(@PathVariable Long id) {
        OperateLog log = operateLogService.getById(id);
        if (log == null) {
            return R.fail(RCode.NOT_FOUND);
        }
        return R.success(toResp(log));
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @RequiresPermission("monitor:operateLog:export")
    public R<List<OperateLogResp>> export(OperateLogQuery query) {
        BusinessStatus status = query.getStatus() != null ? BusinessStatus.fromCode(query.getStatus()) : null;
        List<OperateLog> logs = operateLogService.exportLogs(
                query.getUserId(), query.getModule(), status,
                query.getStartTime(), query.getEndTime());
        return R.success(logs.stream().map(this::toResp).toList());
    }

    @Operation(summary = "清理操作日志")
    @DeleteMapping
    @RequiresPermission("monitor:operateLog:delete")
    public R<Integer> clean(
            @Parameter(description = "保留天数，最少保留7天")
            @RequestParam(defaultValue = "30") @Min(value = 7, message = "保留天数不能少于7天") int days
    ) {
        return R.success(operateLogService.cleanLogs(days));
    }

    private OperateLogResp toResp(OperateLog log) {
        OperateLogResp resp = new OperateLogResp();
        BeanUtils.copyProperties(log, resp);
        return resp;
    }
}
