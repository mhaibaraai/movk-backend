/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.log.OnlineUserQuery;
import com.movk.dto.log.OnlineUserResp;
import com.movk.entity.OnlineUser;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 在线用户管理
 */
@Tag(name = "在线用户管理", description = "在线用户监控相关接口")
@RestController
@RequestMapping("/api/monitor/online-users")
@RequiredArgsConstructor
@Validated
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "分页查询在线用户")
    @GetMapping
    @RequiresPermission("monitor:online:list")
    public R<Page<OnlineUserResp>> list(
            OnlineUserQuery query,
            @PageableDefault(sort = "loginTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OnlineUser> onlinePage = onlineUserService.listOnlineUsers(
                query.getUsername(), query.getLoginIp(), pageable);
        return R.success(onlinePage.map(this::toResp));
    }

    @Operation(summary = "查询所有在线用户")
    @GetMapping("/all")
    @RequiresPermission("monitor:online:list")
    public R<List<OnlineUserResp>> listAll() {
        List<OnlineUser> onlineUsers = onlineUserService.listAllOnlineUsers();
        return R.success(onlineUsers.stream().map(this::toResp).toList());
    }

    @Operation(summary = "统计在线用户数")
    @GetMapping("/count")
    @RequiresPermission("monitor:online:list")
    public R<Long> count() {
        return R.success(onlineUserService.countOnlineUsers());
    }

    @Operation(summary = "强制下线（通过会话ID）")
    @DeleteMapping("/sessions/{sessionId}")
    @RequiresPermission("monitor:online:forceLogout")
    public R<Void> forceLogoutBySession(@PathVariable String sessionId) {
        onlineUserService.forceOfflineBySessionId(sessionId);
        return R.ok();
    }

    @Operation(summary = "强制下线（通过用户ID，踢出所有会话）")
    @DeleteMapping("/users/{userId}")
    @RequiresPermission("monitor:online:forceLogout")
    public R<Void> forceLogoutByUser(@PathVariable UUID userId) {
        onlineUserService.forceOfflineByUserId(userId);
        return R.ok();
    }

    @Operation(summary = "批量强制下线")
    @DeleteMapping("/sessions")
    @RequiresPermission("monitor:online:forceLogout")
    public R<Void> batchForceLogout(@RequestBody List<String> sessionIds) {
        sessionIds.forEach(onlineUserService::forceOfflineBySessionId);
        return R.ok();
    }

    @Operation(summary = "查询用户的所有会话")
    @GetMapping("/users/{userId}/sessions")
    @RequiresPermission("monitor:online:list")
    public R<List<OnlineUserResp>> listUserSessions(@PathVariable UUID userId) {
        List<OnlineUser> sessions = onlineUserService.listUserSessions(userId);
        return R.success(sessions.stream().map(this::toResp).toList());
    }

    private OnlineUserResp toResp(OnlineUser onlineUser) {
        OnlineUserResp resp = new OnlineUserResp();
        BeanUtils.copyProperties(onlineUser, resp);
        return resp;
    }
}
