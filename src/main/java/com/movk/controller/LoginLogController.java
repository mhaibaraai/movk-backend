/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.base.result.RCode;
import com.movk.common.enums.BusinessStatus;
import com.movk.dto.log.LoginLogQuery;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理
 */
@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/api/monitor/login-logs")
@RequiredArgsConstructor
@Validated
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping
    @RequiresPermission("monitor:loginLog:list")
    public R<Page<LoginLogResp>> list(
            LoginLogQuery query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        BusinessStatus status = query.getStatus() != null ? BusinessStatus.fromCode(query.getStatus()) : null;
        Page<LoginLog> logPage = loginLogService.listLoginLogs(
                query.getUsername(), query.getLoginIp(), status,
                query.getStartTime(), query.getEndTime(), pageable);
        return R.success(logPage.map(this::toResp));
    }

    @Operation(summary = "查询登录日志详情")
    @GetMapping("/{id}")
    @RequiresPermission("monitor:loginLog:query")
    public R<LoginLogResp> getById(@PathVariable Long id) {
        LoginLog log = loginLogService.getById(id);
        if (log == null) {
            return R.fail(RCode.NOT_FOUND);
        }
        return R.success(toResp(log));
    }

    @Operation(summary = "导出登录日志")
    @GetMapping("/export")
    @RequiresPermission("monitor:loginLog:export")
    public R<List<LoginLogResp>> export(LoginLogQuery query) {
        BusinessStatus status = query.getStatus() != null ? BusinessStatus.fromCode(query.getStatus()) : null;
        List<LoginLog> logs = loginLogService.exportLogs(
                query.getUsername(), query.getLoginIp(), status,
                query.getStartTime(), query.getEndTime());
        return R.success(logs.stream().map(this::toResp).toList());
    }

    @Operation(summary = "清理登录日志")
    @DeleteMapping
    @RequiresPermission("monitor:loginLog:delete")
    public R<Integer> clean(
            @Parameter(description = "保留天数，最少保留7天")
            @RequestParam(defaultValue = "90") @Min(value = 7, message = "保留天数不能少于7天") int days
    ) {
        return R.success(loginLogService.cleanLogs(days));
    }

    private LoginLogResp toResp(LoginLog log) {
        LoginLogResp resp = new LoginLogResp();
        BeanUtils.copyProperties(log, resp);
        return resp;
    }
}
