/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.controller.facade;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.common.enums.LoginType;
import com.movk.controller.vo.UserInfoVO;
import com.movk.dto.menu.MenuTreeResp;
import com.movk.entity.User;
import com.movk.repository.UserRepository;
import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import com.movk.security.service.LoginUserDetailsService;
import com.movk.security.service.PermissionService;
import com.movk.security.service.TokenService;
import com.movk.service.LoginLogService;
import com.movk.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthAppService {

    private final UserRepository userRepository;
    private final LoginUserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final PermissionService permissionService;
    private final MenuService menuService;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogService loginLogService;

    public AuthTokensDTO loginAndIssueTokens(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            // 记录登录失败日志（用户名使用邮箱，因为此时可能找不到用户）
            loginLogService.recordLoginFailure(email, LoginType.PASSWORD, "用户名或密码错误");
            throw new BusinessException(RCode.INVALID_CREDENTIALS);
        }

        switch (user.getStatus()) {
            case DISABLED -> {
                loginLogService.recordLoginFailure(user.getUsername(), LoginType.PASSWORD, "用户已被禁用");
                throw new BusinessException(RCode.USER_DISABLED);
            }
            case LOCKED, DELETED -> {
                loginLogService.recordLoginFailure(user.getUsername(), LoginType.PASSWORD, "用户已被锁定或删除");
                throw new BusinessException(RCode.FORBIDDEN);
            }
            case ACTIVE -> {}
            default -> {
                loginLogService.recordLoginFailure(user.getUsername(), LoginType.PASSWORD, "用户状态异常");
                throw new BusinessException(RCode.BUSINESS_ERROR);
            }
        }

        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(user.getUsername());
        AuthTokensDTO tokens = tokenService.generateTokens(loginUser);

        // 更新最后登录时间
        user.setLoginDate(java.time.OffsetDateTime.now());
        userRepository.save(user);

        // 记录登录成功日志
        loginLogService.recordLoginSuccess(user.getUsername(), user.getId(), LoginType.PASSWORD, "登录成功");

        return tokens;
    }

    public UserInfoVO buildUserInfo(LoginUser currentUser) {
        List<String> roles = currentUser.getRoles();
        List<String> permissions = permissionService.getPermissionsByRoles(roles);
        List<MenuTreeResp> menus = menuService.getUserMenuTree(currentUser.getId());

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BusinessException(RCode.USER_NOT_FOUND));

        return UserInfoVO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .nickname(currentUser.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .permissions(permissions)
                .menus(menus)
                .build();
    }
}
