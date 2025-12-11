/*
 * @Author yixuanmiao
 * @Date 2025/09/02 09:39
 */

package com.movk.controller;

import com.movk.base.config.JwtHeaderProperties;
import com.movk.base.exception.BusinessException;
import com.movk.base.result.R;
import com.movk.base.result.RCode;
import com.movk.controller.vo.UserInfoVO;
import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import com.movk.security.service.CurrentUserService;
import com.movk.controller.facade.AuthAppService;
import com.movk.security.service.PermissionService;
import com.movk.security.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 * 提供用户登录、用户信息获取、权限检查等接口
 */
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final TokenService tokenService;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;
    private final AuthAppService authAppService;
    private final JwtHeaderProperties jwtHeaderProperties;

    public AuthController(TokenService tokenService,
                         CurrentUserService currentUserService,
                         PermissionService permissionService,
                         AuthAppService authAppService,
                         JwtHeaderProperties jwtHeaderProperties) {
        this.tokenService = tokenService;
        this.currentUserService = currentUserService;
        this.permissionService = permissionService;
        this.authAppService = authAppService;
        this.jwtHeaderProperties = jwtHeaderProperties;
    }

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 认证令牌信息
     */
    @Operation(summary = "用户登录", description = "通过邮箱和密码进行登录认证")
    @PostMapping("/login")
    public R<AuthTokensDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthTokensDTO tokens = authAppService.loginAndIssueTokens(loginRequest.email(), loginRequest.password());
            return R.success(tokens);
        } catch (BusinessException be) {
            log.warn("登录失败: {}", be.getMessage());
            return R.fail(be.getCode());
        } catch (Exception e) {
            log.error("登录过程中发生未预期的错误", e);
            return R.fail(RCode.INTERNAL_ERROR);
        }
    }

    /**
     * 用户登出
     * 
     * @return 操作结果
     */
    @Operation(summary = "用户登出", description = "撤销当前用户的认证令牌")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public R<Void> logout(@RequestHeader("${jwt.header.name}") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith(jwtHeaderProperties.getPrefix())) {
                String token = authHeader.substring(jwtHeaderProperties.getPrefix().length());
                tokenService.revokeToken(token);
            }
            return R.ok();
        } catch (Exception e) {
            return R.fail(RCode.INTERNAL_ERROR);
        }
    }

    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息，包括角色和权限")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public R<UserInfoVO> getUserInfo() {
        try {
            LoginUser currentUser = currentUserService.getCurrentUser();
            UserInfoVO userInfo = authAppService.buildUserInfo(currentUser);
            return R.success(userInfo);
        } catch (BusinessException be) {
            log.warn("获取用户信息失败: {}", be.getMessage());
            return R.fail(be.getCode());
        } catch (Exception e) {
            log.warn("获取用户信息过程中发生未预期的错误", e);
            return R.fail(RCode.INTERNAL_ERROR);
        }
    }

    /**
     * 检查用户权限
     * 
     * @param permissionCode 权限代码
     * @return 是否有权限
     */
    @Operation(summary = "检查权限", description = "检查当前用户是否具有指定权限")
    @GetMapping("/check-permission/{permissionCode}")
    @PreAuthorize("isAuthenticated()")
    public R<Boolean> checkPermission(@PathVariable String permissionCode) {
        try {
            List<String> roles = currentUserService.getCurrentUserRoles();
            boolean hasPermission = permissionService.hasPermission(roles, permissionCode);
            return R.success(hasPermission);

        } catch (Exception e) {
            return R.success(false);
        }
    }

    /**
     * 刷新令牌
     * 
     * @param authHeader 认证头
     * @return 新的令牌信息
     */
    @Operation(summary = "刷新令牌", description = "使用当前令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public R<AuthTokensDTO> refreshToken(@RequestHeader("${jwt.header.name}") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith(jwtHeaderProperties.getPrefix())) {
                return R.fail(RCode.TOKEN_INVALID);
            }

            String token = authHeader.substring(jwtHeaderProperties.getPrefix().length());
            AuthTokensDTO newTokens = tokenService.refreshToken(token);
            return R.success(newTokens);

        } catch (Exception e) {
            return R.fail(RCode.TOKEN_EXPIRED);
        }
    }

    /**
     * 登录请求DTO
     */
    public record LoginRequest(
            @NotBlank(message = "邮箱不能为空") String email,
            @NotBlank(message = "密码不能为空") String password
    ){}
}
