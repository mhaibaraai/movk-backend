/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.base.result.RCode;
import com.movk.common.enums.BusinessStatus;
import com.movk.dto.log.LoginLogResp;
import com.movk.entity.LoginLog;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 登录日志管理
 */
@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/system/loginlog")
@RequiredArgsConstructor
@Validated
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping("/list")
    @RequiresPermission("monitor:loginlog:list")
    public R<Page<LoginLogResp>> list(
        @Parameter(description = "用户名") @RequestParam(required = false) String username,
        @Parameter(description = "登录IP") @RequestParam(required = false) String loginIp,
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

        Page<LoginLog> logPage = loginLogService.listLoginLogs(
            username, loginIp, businessStatus, startTime, endTime, pageable);

        Page<LoginLogResp> respPage = logPage.map(this::toResp);
        return R.success(respPage);
    }

    @Operation(summary = "查询登录日志详情")
    @GetMapping("/{id}")
    @RequiresPermission("monitor:loginlog:query")
    public R<LoginLogResp> getInfo(@PathVariable Long id) {
        LoginLog log = loginLogService.getById(id);
        if (log == null) {
            return R.fail(RCode.NOT_FOUND);
        }
        return R.success(toResp(log));
    }

    @Operation(summary = "导出登录日志")
    @GetMapping("/export")
    @RequiresPermission("monitor:loginlog:export")
    public R<List<LoginLogResp>> export(
        @Parameter(description = "用户名") @RequestParam(required = false) String username,
        @Parameter(description = "登录IP") @RequestParam(required = false) String loginIp,
        @Parameter(description = "状态") @RequestParam(required = false) Short status,
        @Parameter(description = "开始时间") @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
        @Parameter(description = "结束时间") @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime
    ) {
        BusinessStatus businessStatus = status != null ? BusinessStatus.fromCode(status) : null;
        List<LoginLog> logs = loginLogService.exportLogs(username, loginIp, businessStatus, startTime, endTime);
        List<LoginLogResp> respList = logs.stream().map(this::toResp).toList();
        return R.success(respList);
    }

    @Operation(summary = "清理登录日志")
    @DeleteMapping("/clean")
    @RequiresPermission("monitor:loginlog:remove")
    public R<Integer> clean(
        @Parameter(description = "保留天数，最少保留7天") 
        @RequestParam(defaultValue = "90") @Min(value = 7, message = "保留天数不能少于7天") int days
    ) {
        int deleted = loginLogService.cleanLogs(days);
        return R.success(deleted);
    }

    /**
     * 实体转 DTO
     */
    private LoginLogResp toResp(LoginLog log) {
        LoginLogResp resp = new LoginLogResp();
        BeanUtils.copyProperties(log, resp);
        return resp;
    }
}
