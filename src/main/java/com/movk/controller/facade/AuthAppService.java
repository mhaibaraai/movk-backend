/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.controller.facade;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.common.enums.LoginType;
import com.movk.common.enums.UserStatus;
import com.movk.controller.vo.UserInfoVO;
import com.movk.dto.menu.MenuTreeResp;
import com.movk.entity.Role;
import com.movk.entity.User;
import com.movk.repository.RoleRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthAppService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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

    @Transactional
    public AuthTokensDTO registerAndIssueTokens(String email, String password, String nickname) {
        // 检查邮箱是否已被使用
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(RCode.BAD_REQUEST, "邮箱已被使用");
        }

        // 生成用户名（从邮箱派生）
        String username = generateUsernameFromEmail(email);

        String defaultRoleCode = "user";

        Role defaultRole = roleRepository.findByCodeIn(List.of(defaultRoleCode))
                .stream()
                .findFirst()
                .orElse(null); // 当前允许无角色注册，可以根据业务需求修改

        // 创建用户
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname != null && !nickname.isBlank() ? nickname : username)
                .email(email)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // 记录注册成功（可选）
        loginLogService.recordLoginSuccess(user.getUsername(), user.getId(), LoginType.PASSWORD, "注册成功");

        // 自动登录并返回 token
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(user.getUsername());
        return tokenService.generateTokens(loginUser);
    }

    /**
     * 从邮箱生成用户名
     * 策略：取邮箱 @ 前的部分，如果冲突则添加随机后缀
     */
    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }
}
