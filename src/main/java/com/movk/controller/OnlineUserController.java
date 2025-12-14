/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.log.OnlineUserResp;
import com.movk.entity.OnlineUser;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 在线用户管理
 */
@Tag(name = "在线用户管理", description = "在线用户监控相关接口")
@RestController
@RequestMapping("/system/online")
@RequiredArgsConstructor
@Validated
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "分页查询在线用户")
    @GetMapping("/list")
    @RequiresPermission("monitor:online:list")
    public R<Page<OnlineUserResp>> list(
        @Parameter(description = "用户名") @RequestParam(required = false) String username,
        @Parameter(description = "登录IP") @RequestParam(required = false) String loginIp,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OnlineUser> onlinePage = onlineUserService.listOnlineUsers(username, loginIp, pageable);
        Page<OnlineUserResp> respPage = onlinePage.map(this::toResp);
        return R.success(respPage);
    }

    @Operation(summary = "查询所有在线用户")
    @GetMapping("/all")
    @RequiresPermission("monitor:online:list")
    public R<List<OnlineUserResp>> listAll() {
        List<OnlineUser> onlineUsers = onlineUserService.listAllOnlineUsers();
        List<OnlineUserResp> respList = onlineUsers.stream().map(this::toResp).toList();
        return R.success(respList);
    }

    @Operation(summary = "统计在线用户数")
    @GetMapping("/count")
    @RequiresPermission("monitor:online:list")
    public R<Long> count() {
        long count = onlineUserService.countOnlineUsers();
        return R.success(count);
    }

    @Operation(summary = "强制下线（通过会话ID）")
    @DeleteMapping("/session/{sessionId}")
    @RequiresPermission("monitor:online:forceLogout")
    public R<Void> forceLogoutBySession(@PathVariable String sessionId) {
        onlineUserService.forceOfflineBySessionId(sessionId);
        return R.ok();
    }

    @Operation(summary = "强制下线（通过用户ID，踢出所有会话）")
    @DeleteMapping("/user/{userId}")
    @RequiresPermission("monitor:online:forceLogout")
    public R<Void> forceLogoutByUser(@PathVariable UUID userId) {
        onlineUserService.forceOfflineByUserId(userId);
        return R.ok();
    }

    @Operation(summary = "批量强制下线")
    @DeleteMapping("/batch")
    @RequiresPermission("monitor:online:forceLogout")
    public R<Void> batchForceLogout(@RequestBody List<String> sessionIds) {
        for (String sessionId : sessionIds) {
            onlineUserService.forceOfflineBySessionId(sessionId);
        }
        return R.ok();
    }

    @Operation(summary = "查询用户的所有会话")
    @GetMapping("/user/{userId}/sessions")
    @RequiresPermission("monitor:online:list")
    public R<List<OnlineUserResp>> listUserSessions(@PathVariable UUID userId) {
        List<OnlineUser> sessions = onlineUserService.listUserSessions(userId);
        List<OnlineUserResp> respList = sessions.stream().map(this::toResp).toList();
        return R.success(respList);
    }

    /**
     * 实体转 DTO
     */
    private OnlineUserResp toResp(OnlineUser onlineUser) {
        OnlineUserResp resp = new OnlineUserResp();
        BeanUtils.copyProperties(onlineUser, resp);
        return resp;
    }
}
