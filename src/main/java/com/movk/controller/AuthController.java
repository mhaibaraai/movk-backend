/*
 * @Author yixuanmiao
 * @Date 2025/09/02 09:39
 */

package com.movk.controller;

import com.movk.base.config.JwtHeaderProperties;
import com.movk.base.result.R;
import com.movk.base.result.RCode;
import com.movk.controller.facade.AuthAppService;
import com.movk.controller.vo.UserInfoVO;
import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
import com.movk.security.service.PermissionService;
import com.movk.security.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 * 提供用户登录、用户信息获取、权限检查等接口
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final TokenService tokenService;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;
    private final AuthAppService authAppService;
    private final JwtHeaderProperties jwtHeaderProperties;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "通过邮箱和密码进行登录认证")
    @PostMapping("/login")
    public R<AuthTokensDTO> login(@Valid @RequestBody LoginRequest request) {
        return R.success(authAppService.loginAndIssueTokens(request.email(), request.password()));
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "撤销当前用户的认证令牌")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public R<Void> logout(@RequestHeader("${jwt.header.name}") String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtHeaderProperties.getPrefix())) {
            String token = authHeader.substring(jwtHeaderProperties.getPrefix().length());
            tokenService.revokeToken(token);
        }
        return R.ok();
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息，包括角色和权限")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public R<UserInfoVO> getUserInfo() {
        LoginUser currentUser = currentUserService.getCurrentUser();
        return R.success(authAppService.buildUserInfo(currentUser));
    }

    /**
     * 检查用户权限
     */
    @Operation(summary = "检查权限", description = "检查当前用户是否具有指定权限")
    @GetMapping("/permissions/{permissionCode}")
    @PreAuthorize("isAuthenticated()")
    public R<Boolean> checkPermission(@PathVariable String permissionCode) {
        List<String> roles = currentUserService.getCurrentUserRoles();
        return R.success(permissionService.hasPermission(roles, permissionCode));
    }

    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用当前令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public R<AuthTokensDTO> refreshToken(@RequestHeader("${jwt.header.name}") String authHeader) {
        if (authHeader == null || !authHeader.startsWith(jwtHeaderProperties.getPrefix())) {
            return R.fail(RCode.TOKEN_INVALID);
        }
        String token = authHeader.substring(jwtHeaderProperties.getPrefix().length());
        return R.success(tokenService.refreshToken(token));
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "新用户通过邮箱和密码进行注册")
    @PostMapping("/register")
    public R<AuthTokensDTO> register(@Valid @RequestBody RegisterRequest request) {
        return R.success(authAppService.registerAndIssueTokens(request.email(), request.password(), request.nickname()));
    }

    /**
     * 登录请求 DTO
     */
    public record LoginRequest(
            @NotBlank(message = "邮箱不能为空") String email,
            @NotBlank(message = "密码不能为空") String password
    ) {}

    /**
     * 注册请求 DTO
     */
    public record RegisterRequest(
            @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            String email,

            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 50, message = "密码长度必须在 6-50 之间")
            String password,

            @Size(max = 50, message = "昵称长度不能超过 50")
            String nickname
    ) {}
}
