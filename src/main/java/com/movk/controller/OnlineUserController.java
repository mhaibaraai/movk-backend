package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.log.OnlineUserQuery;
import com.movk.dto.log.OnlineUserResp;
import com.movk.entity.RefreshToken;
import com.movk.repository.RefreshTokenRepository;
import com.movk.security.annotation.RequiresPermission;
import com.movk.security.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 在线用户管理
 * 基于 RefreshToken 实现会话管理
 */
@Tag(name = "在线用户管理", description = "在线用户监控相关接口")
@RestController
@RequestMapping("/api/monitor/online-users")
@RequiredArgsConstructor
@Validated
public class OnlineUserController {

    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Operation(summary = "分页查询在线用户")
    @GetMapping
    @RequiresPermission("monitor:online:list")
    public R<Page<OnlineUserResp>> list(
            OnlineUserQuery query,
            @PageableDefault(sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RefreshToken> page = refreshTokenRepository.findAllActive(OffsetDateTime.now(), pageable);
        return R.success(page.map(this::toResp));
    }

    @Operation(summary = "统计在线用户数")
    @GetMapping("/count")
    @RequiresPermission("monitor:online:list")
    public R<Long> count() {
        return R.success(refreshTokenRepository.countAllActive(OffsetDateTime.now()));
    }

    @Operation(summary = "强制下线（通过 RefreshToken ID）")
    @DeleteMapping("/sessions/{tokenId}")
    @RequiresPermission("monitor:online:kick")
    public R<Void> forceLogoutBySession(@PathVariable UUID tokenId) {
        refreshTokenRepository.findById(tokenId).ifPresent(token -> {
            token.revoke("管理员强制下线");
            refreshTokenRepository.save(token);
        });
        return R.ok();
    }

    @Operation(summary = "强制下线（通过用户 ID，踢出所有会话）")
    @DeleteMapping("/users/{userId}")
    @RequiresPermission("monitor:online:kick")
    public R<Void> forceLogoutByUser(@PathVariable UUID userId) {
        tokenService.revokeAllUserTokens(userId, "管理员强制下线");
        return R.ok();
    }

    @Operation(summary = "批量强制下线")
    @DeleteMapping("/sessions")
    @RequiresPermission("monitor:online:kick")
    public R<Void> batchForceLogout(@RequestBody List<UUID> tokenIds) {
        tokenIds.forEach(tokenId ->
            refreshTokenRepository.findById(tokenId).ifPresent(token -> {
                token.revoke("管理员批量强制下线");
                refreshTokenRepository.save(token);
            })
        );
        return R.ok();
    }

    @Operation(summary = "查询用户的所有会话")
    @GetMapping("/users/{userId}/sessions")
    @RequiresPermission("monitor:online:list")
    public R<List<OnlineUserResp>> listUserSessions(@PathVariable UUID userId) {
        List<RefreshToken> sessions = tokenService.getUserActiveSessions(userId);
        return R.success(sessions.stream().map(this::toResp).toList());
    }

    private OnlineUserResp toResp(RefreshToken token) {
        return OnlineUserResp.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .username(token.getUsername())
                .deviceInfo(token.getDeviceInfo())
                .clientIp(token.getClientIp())
                .issuedAt(token.getIssuedAt())
                .expiresAt(token.getExpiresAt())
                .lastUsedAt(token.getLastUsedAt())
                .build();
    }
}
